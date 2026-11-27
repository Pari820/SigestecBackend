package pe.edu.sigestec.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Genera códigos visibles consecutivos mediante una fila de contador bloqueada
 * dentro de la misma transacción que registra el documento. Si el registro falla,
 * el incremento también se revierte; por eso no consume números por rollback.
 */
@Service
public class CorrelativoDocumentoService {
    private final EntityManager entityManager;

    public CorrelativoDocumentoService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente(String tipoDocumento, String prefijo, int anio, int digitos) {
        return siguienteInterno(tipoDocumento, prefijo, anio, digitos, true);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguienteSinAnio(String tipoDocumento, String prefijo, int digitos) {
        return siguienteInterno(tipoDocumento, prefijo, 0, digitos, false);
    }

    /**
     * Genera un correlativo anual y crea el contador del año si todavía no existe.
     * La fila centinela (ANIO=0) serializa la inicialización para evitar carreras
     * cuando dos registros del mismo tipo llegan en el primer uso de un año.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String siguienteConAnioDinamico(String tipoDocumento, String prefijo, int anio, int digitos) {
        if (anio < 1) {
            throw new IllegalArgumentException("El año del documento no es válido");
        }
        try {
            entityManager.createNativeQuery("""
                    SELECT ULTIMO_NUMERO
                    FROM CORRELATIVO_DOCUMENTO
                    WHERE TIPO_DOCUMENTO = :tipo AND ANIO = 0
                    FOR UPDATE
                    """)
                    .setParameter("tipo", tipoDocumento)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new IllegalStateException("No existe el contador base " + tipoDocumento
                    + ". Ejecuta el script Oracle 22_NUMERACION_ACTAS_CONFORMIDAD.sql.", ex);
        }

        entityManager.createNativeQuery("""
                MERGE INTO CORRELATIVO_DOCUMENTO destino
                USING (SELECT :tipo AS TIPO_DOCUMENTO, :anio AS ANIO FROM DUAL) origen
                   ON (destino.TIPO_DOCUMENTO = origen.TIPO_DOCUMENTO AND destino.ANIO = origen.ANIO)
                WHEN NOT MATCHED THEN
                    INSERT (TIPO_DOCUMENTO, ANIO, ULTIMO_NUMERO)
                    VALUES (origen.TIPO_DOCUMENTO, origen.ANIO, 0)
                """)
                .setParameter("tipo", tipoDocumento)
                .setParameter("anio", anio)
                .executeUpdate();

        return siguienteInterno(tipoDocumento, prefijo, anio, digitos, true);
    }

    /** Asigna un número público persistente sin año (clientes, tipos de servicio y personal).
     *  Requiere una fila de contador previamente inicializada en Oracle.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public long siguienteNumeroSinAnio(String tipoDocumento) {
        if (tipoDocumento == null || tipoDocumento.isBlank()) {
            throw new IllegalArgumentException("Tipo de registro obligatorio");
        }
        final Number anterior;
        try {
            anterior = (Number) entityManager.createNativeQuery("""
                    SELECT ULTIMO_NUMERO FROM CORRELATIVO_DOCUMENTO
                    WHERE TIPO_DOCUMENTO = :tipo AND ANIO = 0
                    FOR UPDATE
                    """)
                    .setParameter("tipo", tipoDocumento)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new IllegalStateException("No existe el contador de " + tipoDocumento
                    + ". Ejecuta el script 23_NUMERACION_CATALOGOS.sql antes de iniciar Spring Boot.", ex);
        }
        long siguiente = anterior.longValue() + 1L;
        int filas = entityManager.createNativeQuery("""
                UPDATE CORRELATIVO_DOCUMENTO SET ULTIMO_NUMERO = :numero
                WHERE TIPO_DOCUMENTO = :tipo AND ANIO = 0
                """)
                .setParameter("numero", siguiente)
                .setParameter("tipo", tipoDocumento)
                .executeUpdate();
        if (filas != 1) throw new IllegalStateException("No se pudo actualizar el contador de " + tipoDocumento);
        return siguiente;
    }

    private String siguienteInterno(String tipoDocumento, String prefijo, int anio, int digitos, boolean incluirAnio) {
        if (tipoDocumento == null || tipoDocumento.isBlank() || prefijo == null || prefijo.isBlank()) {
            throw new IllegalArgumentException("El tipo y prefijo del documento son obligatorios");
        }
        if (digitos < 1 || digitos > 12) {
            throw new IllegalArgumentException("La longitud del correlativo no es válida");
        }

        final Number actual;
        try {
            actual = (Number) entityManager.createNativeQuery("""
                    SELECT ULTIMO_NUMERO
                    FROM CORRELATIVO_DOCUMENTO
                    WHERE TIPO_DOCUMENTO = :tipo AND ANIO = :anio
                    FOR UPDATE
                    """)
                    .setParameter("tipo", tipoDocumento)
                    .setParameter("anio", anio)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new IllegalStateException("No existe el contador " + tipoDocumento + " para el año " + anio
                    + ". Ejecuta el script Oracle 21_NUMERACION_CORRELATIVA_DOCUMENTOS.sql.", ex);
        }

        long siguiente = actual.longValue() + 1L;
        int filas = entityManager.createNativeQuery("""
                UPDATE CORRELATIVO_DOCUMENTO
                SET ULTIMO_NUMERO = :nuevo
                WHERE TIPO_DOCUMENTO = :tipo AND ANIO = :anio
                """)
                .setParameter("nuevo", siguiente)
                .setParameter("tipo", tipoDocumento)
                .setParameter("anio", anio)
                .executeUpdate();
        if (filas != 1) {
            throw new IllegalStateException("No se pudo actualizar el contador " + tipoDocumento);
        }

        String correlativo = String.format(Locale.ROOT, "%0" + digitos + "d", siguiente);
        return incluirAnio ? prefijo + "-" + anio + "-" + correlativo : prefijo + "-" + correlativo;
    }
}
