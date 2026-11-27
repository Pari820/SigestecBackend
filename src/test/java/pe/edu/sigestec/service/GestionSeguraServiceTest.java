
package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.storage.ArchivoStorageService;

@ExtendWith(MockitoExtension.class)
class GestionSeguraServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private ApplicationEventPublisher auditoriaEventos;

    @Mock
    private ArchivoStorageService storage;

    @Mock
    private TypedQuery<Long> consulta;

    @InjectMocks
    private GestionSeguraService gestion;

    @BeforeEach
    void configurar() {
        gestion = new GestionSeguraService(storage);

        ReflectionTestUtils.setField(gestion, "em", em);
        ReflectionTestUtils.setField(
            gestion, "auditoriaEventos", auditoriaEventos
        );

        autenticar("GERENTE");
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "usuario-prueba",
                null,
                java.util.List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
                )
            )
        );
    }

    private void consultaSinRegistros() {
        when(em.createQuery(anyString(), eq(Long.class)))
            .thenReturn(consulta);

        when(consulta.setParameter(anyString(), any()))
            .thenReturn(consulta);

        when(consulta.getSingleResult()).thenReturn(0L);
    }

    // 1. Editar correctamente una solicitud.

    @Test
    void editarSolicitudCorrectamente() {

        SolicitudServicio solicitud = mock(SolicitudServicio.class);

        when(em.find(SolicitudServicio.class, 1L))
            .thenReturn(solicitud);

        when(solicitud.getEstado()).thenReturn("PENDIENTE");
        when(solicitud.getDescripcion()).thenReturn("Descripción anterior");

        consultaSinRegistros();

        Map<String, String> resultado = gestion.editar(
            "solicitudes",
            1L,
            Map.of("descripcion", "Descripción corregida")
        );

        verify(solicitud).setDescripcion("Descripción corregida");
        verify(em).flush();
        verify(auditoriaEventos).publishEvent(
            org.mockito.ArgumentMatchers.<Object>any()
        );

        assertEquals(
            "Cambios guardados en Oracle",
            resultado.get("mensaje")
        );
    }

    // 2. Impedir modificaciones sin campos.

    @Test
    void rechazaEdicionSinCampos() {

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                1L,
                Map.of()
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 3. Impedir la modificación de campos protegidos.

    @Test
    void rechazaCamposNoPermitidos() {

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                1L,
                Map.of("id", 100L)
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 4. Impedir modificaciones con un rol no autorizado.

    @Test
    void rechazaUsuarioSinPermisos() {

        autenticar("SSOMA");

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                1L,
                Map.of("descripcion", "Cambio no autorizado")
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 5. Validar identificadores inválidos.

    @Test
    void rechazaIdentificadorNulo() {

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                null,
                Map.of("descripcion", "Prueba")
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 6. Impedir que una solicitud con cotización sea modificada.

    @Test
    void rechazaEdicionDeSolicitudConCotizacion() {

        SolicitudServicio solicitud = mock(SolicitudServicio.class);

        when(em.find(SolicitudServicio.class, 1L))
            .thenReturn(solicitud);

        when(solicitud.getEstado()).thenReturn("PENDIENTE");

        when(em.createQuery(anyString(), eq(Long.class)))
            .thenReturn(consulta);

        when(consulta.setParameter(anyString(), any()))
            .thenReturn(consulta);

        when(consulta.getSingleResult()).thenReturn(1L);

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                1L,
                Map.of("descripcion", "Cambio")
            )
        );

        verify(solicitud, never()).setDescripcion(anyString());
    }

    // 7. Impedir descripciones obligatorias vacías.

    @Test
    void rechazaDescripcionVacia() {

        SolicitudServicio solicitud = mock(SolicitudServicio.class);

        when(em.find(SolicitudServicio.class, 1L))
            .thenReturn(solicitud);

        when(solicitud.getEstado()).thenReturn("PENDIENTE");

        consultaSinRegistros();

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "solicitudes",
                1L,
                Map.of("descripcion", "   ")
            )
        );
    }

    // 8. Eliminar una solicitud independiente.

    @Test
    void eliminaSolicitudSinDependencias() {

        SolicitudServicio solicitud = mock(SolicitudServicio.class);

        when(em.find(SolicitudServicio.class, 1L))
            .thenReturn(solicitud);

        when(solicitud.getEstado()).thenReturn("PENDIENTE");

        consultaSinRegistros();

        Map<String, String> resultado = gestion.retirar(
            "solicitudes",
            1L,
            "eliminar"
        );

        verify(em).remove(solicitud);
        verify(em).flush();

        assertEquals(
            "Solicitud sin dependencias eliminada",
            resultado.get("mensaje")
        );
    }

    // 9. Cancelar una solicitud conservando su historial.

    @Test
    void cancelaSolicitudSinEliminarla() {

        SolicitudServicio solicitud = mock(SolicitudServicio.class);

        when(em.find(SolicitudServicio.class, 1L))
            .thenReturn(solicitud);

        when(solicitud.getEstado()).thenReturn("PENDIENTE");

        consultaSinRegistros();

        gestion.retirar(
            "solicitudes",
            1L,
            "cancelar"
        );

        verify(solicitud).setEstado("CANCELADA");
        verify(em, never()).remove(solicitud);
        verify(em).flush();
    }

    // 10. Rechazar acciones desconocidas.

    @Test
    void rechazaAccionDesconocida() {

        assertThrows(
            NegocioException.class,
            () -> gestion.retirar(
                "solicitudes",
                1L,
                "modificar"
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 11. Reactivar un cliente inactivo.

    @Test
    void reactivaClienteInactivo() {

        PersonaJuridica cliente = mock(PersonaJuridica.class);

        when(em.find(PersonaJuridica.class, 1L))
            .thenReturn(cliente);

        when(cliente.getEstado()).thenReturn("I");

        Map<String, String> resultado = gestion.reactivar(
            "clientes",
            1L
        );

        verify(cliente).setEstado("A");
        verify(em).flush();
        verify(auditoriaEventos).publishEvent(
            org.mockito.ArgumentMatchers.<Object>any()
        );

        assertEquals(
            "Cliente reactivado",
            resultado.get("mensaje")
        );
    }

    // 12. Impedir reactivar un cliente que ya está activo.

    @Test
    void rechazaReactivarClienteActivo() {

        PersonaJuridica cliente = mock(PersonaJuridica.class);

        when(em.find(PersonaJuridica.class, 1L))
            .thenReturn(cliente);

        when(cliente.getEstado()).thenReturn("A");

        assertThrows(
            NegocioException.class,
            () -> gestion.reactivar("clientes", 1L)
        );

        verify(cliente, never()).setEstado(anyString());
    }

    // 13. Impedir la edición directa de documentos históricos.

    @Test
    void rechazaEdicionDirectaDeConformidades() {

        autenticar("SUPERVISOR");

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "conformidades",
                1L,
                Map.of("diagnostico", "Cambio")
            )
        );

        verify(em, never()).find(any(), any());
    }

    // 14. Rechazar módulos inexistentes.

    @Test
    void rechazaModuloNoReconocido() {

        assertThrows(
            NegocioException.class,
            () -> gestion.editar(
                "modulo-inexistente",
                1L,
                Map.of("descripcion", "Prueba")
            )
        );

        verify(em, never()).find(any(), any());
    }


    // Cobertura adicional de rutas históricas y módulos que requieren flujos especializados.

    @Test
    void rechazaAccionDeRetiroDesconocidaAntesDeConsultarLaBase() {
        assertThrows(NegocioException.class,
            () -> gestion.retirar("solicitudes", 1L, "archivar"));
        verifyNoInteractions(em);
    }

    @Test
    void rechazaReactivacionDeModuloHistorico() {
        assertThrows(NegocioException.class,
            () -> gestion.reactivar("conformidades", 1L));
        verifyNoInteractions(em);
    }

    @Test
    void impideEditarOrdenesDeTrabajoDesdeGestionGenerica() {
        assertThrows(NegocioException.class,
            () -> gestion.editar("ordenes-trabajo", 1L, Map.of("descripcion", "Cambio")));
        verifyNoInteractions(em);
    }

    @Test
    void rechazaModuloNoReconocidoEnEdicion() {
        assertThrows(NegocioException.class,
            () -> gestion.editar("modulo-inexistente", 1L, Map.of("campo", "valor")));
        verifyNoInteractions(em);
    }

    // Cobertura adicional: edición de los módulos comerciales y operativos.

    private void consultaConResultados(Long... resultados) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(consulta);
        when(consulta.setParameter(anyString(), any())).thenReturn(consulta);
        int[] indice = {0};
        when(consulta.getSingleResult()).thenAnswer(invocacion ->
            resultados[Math.min(indice[0]++, resultados.length - 1)]);
    }

    @Test
    void editaClienteActivo() {
        autenticar("GERENTE");
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        when(em.find(PersonaJuridica.class, 2L)).thenReturn(cliente);
        when(cliente.getEstado()).thenReturn("A");

        gestion.editar("clientes", 2L, Map.of(
            "razonSocial", "Empresa corregida",
            "telefono", "999888777",
            "correo", "contacto@empresa.pe"));

        verify(cliente).setRazonSocial("Empresa corregida");
        verify(cliente).setTelefono("999888777");
        verify(cliente).setCorreo("contacto@empresa.pe");
        verify(em).flush();
    }

    @Test
    void editaTipoServicioSinDuplicados() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.catalogos.TipoServicio tipo =
            mock(pe.edu.sigestec.entity.catalogos.TipoServicio.class);
        when(em.find(pe.edu.sigestec.entity.catalogos.TipoServicio.class, 3L)).thenReturn(tipo);
        when(tipo.getEstado()).thenReturn("A");
        when(tipo.getNombre()).thenReturn("Mantenimiento");
        when(tipo.getDescripcion()).thenReturn("Anterior");
        consultaConResultados(0L);

        gestion.editar("tipos-servicio", 3L,
            Map.of("nombre", "Instalación", "descripcion", "Descripción nueva"));

        verify(tipo).setNombre("Instalación");
        verify(tipo).setDescripcion("Descripción nueva");
    }

    @Test
    void editaPersonalActivo() {
        autenticar("SUPERVISOR");
        pe.edu.sigestec.entity.personal.Empleado empleado =
            mock(pe.edu.sigestec.entity.personal.Empleado.class);
        pe.edu.sigestec.entity.catalogos.PersonaNatural persona =
            mock(pe.edu.sigestec.entity.catalogos.PersonaNatural.class);
        when(em.find(pe.edu.sigestec.entity.personal.Empleado.class, 4L)).thenReturn(empleado);
        when(empleado.getEstado()).thenReturn("A");
        when(empleado.getPersonaNatural()).thenReturn(persona);

        gestion.editar("personal", 4L, Map.of("nombre", "Ana María Pérez"));

        verify(persona).setNombres("Ana María");
        verify(persona).setApellidos("Pérez");
    }

    @Test
    void editaMaterialActivoSinMovimientos() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.Material material =
            mock(pe.edu.sigestec.entity.almacen.Material.class);
        when(em.find(pe.edu.sigestec.entity.almacen.Material.class, 5L)).thenReturn(material);
        when(material.getEstado()).thenReturn("A");
        when(material.getNombre()).thenReturn("Cable");
        when(material.getUnidadMedida()).thenReturn("m");
        when(material.getStockMinimo()).thenReturn(java.math.BigDecimal.ONE);
        consultaConResultados(0L, 0L);

        gestion.editar("materiales", 5L, Map.of(
            "nombre", "Cable eléctrico",
            "unidadMedida", "rollo",
            "stockMinimo", "2.50"));

        verify(material).setNombre("Cable eléctrico");
        verify(material).setUnidadMedida("rollo");
        verify(material).setStockMinimo(new java.math.BigDecimal("2.50"));
    }

    @Test
    void editaHerramientaActiva() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.HerramientaEquipo herramienta =
            mock(pe.edu.sigestec.entity.almacen.HerramientaEquipo.class);
        when(em.find(pe.edu.sigestec.entity.almacen.HerramientaEquipo.class, 6L)).thenReturn(herramienta);
        when(herramienta.getEstado()).thenReturn("A");
        when(herramienta.getNombre()).thenReturn("Taladro");
        when(herramienta.getDescripcion()).thenReturn("Anterior");

        gestion.editar("herramientas", 6L,
            Map.of("nombre", "Taladro industrial", "descripcion", "Equipo nuevo"));

        verify(herramienta).setNombre("Taladro industrial");
        verify(herramienta).setDescripcion("Equipo nuevo");
    }

    @Test
    void editaOrdenCompraRegistradaSinOrdenTrabajo() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.compras.OrdenCompra compra =
            mock(pe.edu.sigestec.entity.compras.OrdenCompra.class);
        when(em.find(pe.edu.sigestec.entity.compras.OrdenCompra.class, 7L)).thenReturn(compra);
        when(compra.getEstado()).thenReturn("REGISTRADA");
        when(compra.getNumeroOrdenCompra()).thenReturn("OC-001");
        consultaConResultados(0L, 0L);

        gestion.editar("compras", 7L, Map.of("numero", "OC-002"));

        verify(compra).setNumeroOrdenCompra("OC-002");
    }

    @Test
    void editaAtsSinAprobacionNiEjecucion() {
        autenticar("SSOMA");
        pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro ats =
            mock(pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro.class);
        pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo orden =
            mock(pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo.class);
        when(em.find(pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro.class, 8L)).thenReturn(ats);
        when(ats.getEstado()).thenReturn("PENDIENTE");
        when(ats.getOrdenTrabajo()).thenReturn(orden);
        when(orden.getId()).thenReturn(80L);
        when(ats.getCondicionesArea()).thenReturn("Condiciones anteriores");
        when(ats.getObservaciones()).thenReturn("Observaciones anteriores");
        consultaConResultados(0L, 0L);

        gestion.editar("ats", 8L,
            Map.of("condicionesArea", "Área despejada", "observaciones", "Sin novedades"));

        verify(ats).setCondicionesArea("Área despejada");
        verify(ats).setObservaciones("Sin novedades");
    }

    @Test
    void editaEjecucionSinInformeTecnico() {
        autenticar("SUPERVISOR");
        pe.edu.sigestec.entity.ejecuciones.EjecucionServicio ejecucion =
            mock(pe.edu.sigestec.entity.ejecuciones.EjecucionServicio.class);
        when(em.find(pe.edu.sigestec.entity.ejecuciones.EjecucionServicio.class, 9L)).thenReturn(ejecucion);
        when(ejecucion.getDescripcion()).thenReturn("Descripción anterior");
        when(ejecucion.getMediciones()).thenReturn("Medición anterior");
        when(ejecucion.getObservaciones()).thenReturn("Observación anterior");
        consultaConResultados(0L);

        gestion.editar("ejecuciones", 9L, Map.of(
            "descripcion", "Trabajo realizado",
            "mediciones", "10 unidades",
            "observaciones", "Prueba satisfactoria"));

        verify(ejecucion).setDescripcion("Trabajo realizado");
        verify(ejecucion).setMediciones("10 unidades");
        verify(ejecucion).setObservaciones("Prueba satisfactoria");
    }

    @Test
    void editaInformeSinConformidad() {
        autenticar("SUPERVISOR");
        pe.edu.sigestec.entity.informes.InformeTecnico informe =
            mock(pe.edu.sigestec.entity.informes.InformeTecnico.class);
        when(em.find(pe.edu.sigestec.entity.informes.InformeTecnico.class, 10L)).thenReturn(informe);
        when(informe.getDiagnostico()).thenReturn("Diagnóstico anterior");
        when(informe.getTrabajosRealizados()).thenReturn("Trabajo anterior");
        when(informe.getConclusiones()).thenReturn("Conclusión anterior");
        when(informe.getRecomendaciones()).thenReturn("Recomendación anterior");
        consultaConResultados(0L);

        gestion.editar("informes", 10L, Map.of(
            "diagnostico", "Diagnóstico actualizado",
            "trabajosRealizados", "Mantenimiento realizado",
            "conclusiones", "Operativo",
            "recomendaciones", "Revisar en 30 días"));

        verify(informe).setDiagnostico("Diagnóstico actualizado");
        verify(informe).setTrabajosRealizados("Mantenimiento realizado");
        verify(informe).setConclusiones("Operativo");
        verify(informe).setRecomendaciones("Revisar en 30 días");
    }

    @Test
    void rechazaTipoServicioDuplicado() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.catalogos.TipoServicio tipo =
            mock(pe.edu.sigestec.entity.catalogos.TipoServicio.class);
        when(em.find(pe.edu.sigestec.entity.catalogos.TipoServicio.class, 3L)).thenReturn(tipo);
        when(tipo.getEstado()).thenReturn("A");
        when(tipo.getNombre()).thenReturn("Mantenimiento");
        consultaConResultados(1L);

        assertThrows(NegocioException.class,
            () -> gestion.editar("tipos-servicio", 3L, Map.of("nombre", "Instalación")));
        verify(tipo, never()).setNombre(anyString());
    }

    @Test
    void rechazaUnidadDeMaterialConMovimientos() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.Material material =
            mock(pe.edu.sigestec.entity.almacen.Material.class);
        when(em.find(pe.edu.sigestec.entity.almacen.Material.class, 5L)).thenReturn(material);
        when(material.getEstado()).thenReturn("A");
        consultaConResultados(1L);

        assertThrows(NegocioException.class,
            () -> gestion.editar("materiales", 5L, Map.of("unidadMedida", "caja")));
        verify(material, never()).setUnidadMedida(anyString());
    }

    @Test
    void rechazaDecimalNegativoEnMaterial() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.Material material =
            mock(pe.edu.sigestec.entity.almacen.Material.class);
        when(em.find(pe.edu.sigestec.entity.almacen.Material.class, 5L)).thenReturn(material);
        when(material.getEstado()).thenReturn("A");
        when(material.getNombre()).thenReturn("Cable");
        when(material.getUnidadMedida()).thenReturn("m");
        when(material.getStockMinimo()).thenReturn(java.math.BigDecimal.ZERO);

        assertThrows(NegocioException.class,
            () -> gestion.editar("materiales", 5L, Map.of("stockMinimo", "-1")));
        verify(material, never()).setStockMinimo(any());
    }

    @Test
    void rechazaNombreDePersonalSinApellido() {
        autenticar("SUPERVISOR");
        pe.edu.sigestec.entity.personal.Empleado empleado =
            mock(pe.edu.sigestec.entity.personal.Empleado.class);
        when(em.find(pe.edu.sigestec.entity.personal.Empleado.class, 4L)).thenReturn(empleado);
        when(empleado.getEstado()).thenReturn("A");
        assertThrows(NegocioException.class,
            () -> gestion.editar("personal", 4L, Map.of("nombre", "Ana")));
    }

    @Test
    void rechazaSolicitudCuandoNoEstaPendiente() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        when(em.find(SolicitudServicio.class, 1L)).thenReturn(solicitud);
        when(solicitud.getEstado()).thenReturn("CANCELADA");
        assertThrows(NegocioException.class,
            () -> gestion.editar("solicitudes", 1L, Map.of("descripcion", "Cambio")));
        verify(solicitud, never()).setDescripcion(anyString());
    }

    @Test
    void rechazaEdicionCuandoLaEntidadNoExiste() {
        when(em.find(SolicitudServicio.class, 999L)).thenReturn(null);
        assertThrows(NegocioException.class,
            () -> gestion.editar("solicitudes", 999L, Map.of("descripcion", "Cambio")));
    }

    @Test
    void rechazaCampoConTipoIncorrecto() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        when(em.find(SolicitudServicio.class, 1L)).thenReturn(solicitud);
        when(solicitud.getEstado()).thenReturn("PENDIENTE");
        consultaSinRegistros();
        assertThrows(NegocioException.class,
            () -> gestion.editar("solicitudes", 1L, Map.of("descripcion", 123)));
    }

    @Test
    void rechazaCampoQueSuperaLongitudMaxima() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        when(em.find(SolicitudServicio.class, 1L)).thenReturn(solicitud);
        when(solicitud.getEstado()).thenReturn("PENDIENTE");
        consultaSinRegistros();
        assertThrows(NegocioException.class,
            () -> gestion.editar("solicitudes", 1L, Map.of("descripcion", "x".repeat(1001))));
    }

    @Test
    void rechazaRolNoAutenticado() {
        SecurityContextHolder.clearContext();
        assertThrows(NegocioException.class,
            () -> gestion.editar("solicitudes", 1L, Map.of("descripcion", "Cambio")));
        verifyNoInteractions(em);
    }

    @Test
    void reactivaSolicitudCanceladaSinCotizacion() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        when(em.find(SolicitudServicio.class, 11L)).thenReturn(solicitud);
        when(solicitud.getEstado()).thenReturn("CANCELADA");
        consultaConResultados(0L);

        Map<String, String> resultado = gestion.reactivar("solicitudes", 11L);

        verify(solicitud).setEstado("PENDIENTE");
        assertEquals("Solicitud reactivada para continuar el proceso", resultado.get("mensaje"));
    }

    @Test
    void cancelaOrdenCompraRegistradaSinOrdenTrabajo() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.compras.OrdenCompra compra =
            mock(pe.edu.sigestec.entity.compras.OrdenCompra.class);
        when(em.find(pe.edu.sigestec.entity.compras.OrdenCompra.class, 12L)).thenReturn(compra);
        when(compra.getEstado()).thenReturn("REGISTRADA");
        consultaConResultados(0L);

        gestion.retirar("compras", 12L, "cancelar");

        verify(compra).setEstado("ANULADA");
        verify(em).flush();
    }

    @Test
    void inactivaClienteSinSolicitudesPendientes() {
        autenticar("GERENTE");
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        when(em.find(PersonaJuridica.class, 13L)).thenReturn(cliente);
        when(cliente.getEstado()).thenReturn("A");
        consultaConResultados(0L);

        gestion.retirar("clientes", 13L, "cancelar");

        verify(cliente).setEstado("I");
    }

    @Test
    void reactivaTipoServicioInactivo() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.catalogos.TipoServicio tipo =
            mock(pe.edu.sigestec.entity.catalogos.TipoServicio.class);
        when(em.find(pe.edu.sigestec.entity.catalogos.TipoServicio.class, 14L)).thenReturn(tipo);
        when(tipo.getEstado()).thenReturn("I");

        gestion.reactivar("tipos-servicio", 14L);

        verify(tipo).setEstado("A");
    }

    @Test
    void reactivaPersonalInactivo() {
        autenticar("SUPERVISOR");
        pe.edu.sigestec.entity.personal.Empleado empleado =
            mock(pe.edu.sigestec.entity.personal.Empleado.class);
        when(em.find(pe.edu.sigestec.entity.personal.Empleado.class, 15L)).thenReturn(empleado);
        when(empleado.getEstado()).thenReturn("I");

        gestion.reactivar("personal", 15L);

        verify(empleado).setEstado("A");
    }

    @Test
    void reactivaMaterialInactivo() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.Material material =
            mock(pe.edu.sigestec.entity.almacen.Material.class);
        when(em.find(pe.edu.sigestec.entity.almacen.Material.class, 16L)).thenReturn(material);
        when(material.getEstado()).thenReturn("I");

        gestion.reactivar("materiales", 16L);

        verify(material).setEstado("A");
    }

    @Test
    void reactivaHerramientaInactiva() {
        autenticar("ALMACENERO");
        pe.edu.sigestec.entity.almacen.HerramientaEquipo herramienta =
            mock(pe.edu.sigestec.entity.almacen.HerramientaEquipo.class);
        when(em.find(pe.edu.sigestec.entity.almacen.HerramientaEquipo.class, 17L)).thenReturn(herramienta);
        when(herramienta.getEstado()).thenReturn("I");

        gestion.reactivar("herramientas", 17L);

        verify(herramienta).setEstado("A");
    }

    @Test
    void rechazaEliminarOrdenCompraEmitida() {
        autenticar("GERENTE");
        pe.edu.sigestec.entity.compras.OrdenCompra compra =
            mock(pe.edu.sigestec.entity.compras.OrdenCompra.class);
        when(em.find(pe.edu.sigestec.entity.compras.OrdenCompra.class, 18L)).thenReturn(compra);

        assertThrows(NegocioException.class,
            () -> gestion.retirar("compras", 18L, "eliminar"));
    }

    @Test
    void rechazaInactivarClienteConSolicitudesPendientes() {
        autenticar("GERENTE");
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        when(em.find(PersonaJuridica.class, 19L)).thenReturn(cliente);
        when(cliente.getEstado()).thenReturn("A");
        consultaConResultados(2L);

        assertThrows(NegocioException.class,
            () -> gestion.retirar("clientes", 19L, "cancelar"));
        verify(cliente, never()).setEstado("I");
    }

    @Test
    void rechazaReactivarSolicitudConEstadoIncorrecto() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        when(em.find(SolicitudServicio.class, 20L)).thenReturn(solicitud);
        when(solicitud.getEstado()).thenReturn("PENDIENTE");
        assertThrows(NegocioException.class,
            () -> gestion.reactivar("solicitudes", 20L));
    }

}
