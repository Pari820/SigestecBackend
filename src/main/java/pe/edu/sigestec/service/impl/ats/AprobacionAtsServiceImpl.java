package pe.edu.sigestec.service.impl.ats;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AprobacionAtsRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.AprobacionAts;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.ats.AprobacionAtsRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.ats.AprobacionAtsService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class AprobacionAtsServiceImpl implements AprobacionAtsService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final AnalisisTrabajoSeguroRepository analisis;
    private final AprobacionAtsRepository aprobaciones;
    private final UsuarioActualService actual;
    private final ArchivoStorageService archivos;
    public AprobacionAtsServiceImpl(AnalisisTrabajoSeguroRepository a, AprobacionAtsRepository r, UsuarioActualService u, ArchivoStorageService f) {
        analisis=a; aprobaciones=r; actual=u; archivos=f;
    }
    @Override
    public AprobacionAts registrar(Long idAts, AprobacionAtsRequestDTO f, MultipartFile acta) {
        AnalisisTrabajoSeguro ats=analisis.findById(idAts).orElseThrow(()->NegocioException.noExiste("ATS no encontrado"));
        if (!"PENDIENTE".equals(ats.getEstado()) && !"ELABORADO".equals(ats.getEstado()))
            throw NegocioException.conflicto("Solo se pueden resolver ATS pendientes de aprobación");
        if (aprobaciones.existsByAnalisisTrabajoSeguroId(idAts)) throw NegocioException.conflicto("ATS ya resuelto");
        if (ats.getFechaRegistro() == null)
            throw NegocioException.conflicto("El ATS no tiene fecha de registro");
        if (ats.getOrdenTrabajo() == null || ats.getOrdenTrabajo().getFechaProgramadaInicio() == null)
            throw NegocioException.conflicto("La OT asociada no tiene fecha de inicio programada");
        java.time.LocalDate fechaInicioTrabajo = ats.getOrdenTrabajo().getFechaProgramadaInicio().toLocalDate();
        java.time.LocalDate ultimaFechaRevision = ReglasFechaServicio.ultimaFechaAprobacionAts(ats.getOrdenTrabajo().getFechaProgramadaInicio());
        if (ats.getFechaRegistro().isAfter(ultimaFechaRevision))
            throw NegocioException.conflicto("El ATS debe registrarse antes del día de inicio programado del trabajo");
        if (f.fechaRevision().isBefore(ats.getFechaRegistro()))
            throw NegocioException.invalido("La fecha de revisión no puede ser anterior al registro del ATS");
        if (!ReglasFechaServicio.fechaRevisionValida(ats.getFechaRegistro(),
                ats.getOrdenTrabajo().getFechaProgramadaInicio(), f.fechaRevision()))
            throw NegocioException.invalido("La revisión debe realizarse antes del día de inicio de la OT (último día válido: " + ultimaFechaRevision + ")");
        if ("APROBADO".equals(f.resultado()) && (acta==null || acta.isEmpty()))
            throw NegocioException.invalido("Adjunta el acta con las firmas o aprobación del cliente");
        if ("RECHAZADO".equals(f.resultado()) && (f.observaciones()==null || f.observaciones().isBlank()))
            throw NegocioException.invalido("Indica el motivo de rechazo del ATS");
        AprobacionAts aprobacion=new AprobacionAts();
        aprobacion.setAnalisisTrabajoSeguro(ats);
        aprobacion.setEmpleadoRegistra(actual.empleado());
        aprobacion.setFechaRevision(f.fechaRevision());
        aprobacion.setSupervisorCliente(f.supervisorCliente().trim());
        aprobacion.setSsomaCliente(f.ssomaCliente().trim());
        aprobacion.setResultado(f.resultado());
        aprobacion.setObservaciones(f.observaciones());
        aprobacion.setUrlActa(archivos.guardar(acta,false,false));
        aprobaciones.saveAndFlush(aprobacion);
        ats.setEstado(f.resultado());
        analisis.save(ats);
        auditoriaEventos.publishEvent(EventoAuditoria.de("ATS_REVISADO", "AprobacionAts", aprobacion.getId(), "Resultado: " + f.resultado()));
        return aprobacion;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.ats.AprobacionAts> listar() {
        return aprobaciones.findAll();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro> pendientes() {
        return analisis.findAll().stream()
            .filter(a -> "PENDIENTE".equals(a.getEstado()) || "ELABORADO".equals(a.getEstado()))
            .toList();
    }

}
