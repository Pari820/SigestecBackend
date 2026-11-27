package pe.edu.sigestec;

import static org.junit.jupiter.api.Assertions.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.asignaciones.AsignacionPersonalRequestDTO;
import pe.edu.sigestec.dto.ats.AnalisisTrabajoSeguroRequestDTO;
import pe.edu.sigestec.dto.ats.AtsEtapaDTO;
import pe.edu.sigestec.dto.cotizaciones.CotizacionRequestDTO;
import pe.edu.sigestec.dto.cotizaciones.DetalleCotizacionRequestDTO;
import pe.edu.sigestec.dto.solicitudes.SolicitudServicioRequestDTO;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.asignaciones.AsignacionPersonalRepository;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.ats.DetalleAnalisisRiesgoRepository;
import pe.edu.sigestec.repository.catalogos.PersonaJuridicaRepository;
import pe.edu.sigestec.repository.catalogos.TipoServicioRepository;
import pe.edu.sigestec.repository.compras.DetalleOrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.CotizacionRepository;
import pe.edu.sigestec.repository.cotizaciones.DetalleCotizacionRepository;
import pe.edu.sigestec.repository.orden_trabajo.DocumentoPlanificacionRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.asignaciones.AsignacionPersonalService;
import pe.edu.sigestec.service.service.ats.AnalisisTrabajoSeguroService;
import pe.edu.sigestec.service.service.compras.OrdenCompraService;
import pe.edu.sigestec.service.service.cotizaciones.CotizacionService;
import pe.edu.sigestec.service.service.orden_trabajo.OrdenTrabajoService;
import pe.edu.sigestec.service.service.solicitudes.SolicitudServicioService;

@Disabled("Requiere una base Oracle exclusiva para pruebas de integración")
@SpringBootTest
@Transactional
class FlujoComercialIntegrationTest {
    @Autowired
    SolicitudServicioService solicitudes;
    @Autowired
    CotizacionService cotizaciones;
    @Autowired
    OrdenCompraService compras;
    @Autowired
    AsignacionPersonalService asignaciones;
    @Autowired
    OrdenTrabajoService programacion;
    @Autowired
    AnalisisTrabajoSeguroService analisis;
    @Autowired
    PersonaJuridicaRepository empresas;
    @Autowired
    TipoServicioRepository tipos;
    @Autowired
    EmpleadoRepository empleados;
    @Autowired
    CotizacionRepository cotizacionesRepo;
    @Autowired
    DetalleCotizacionRepository detallesCotizacion;
    @Autowired
    DetalleOrdenCompraRepository detallesCompra;
    @Autowired
    AsignacionPersonalRepository asignacionesRepo;
    @Autowired
    DocumentoPlanificacionRepository documentos;
    @Autowired
    DetalleAnalisisRiesgoRepository detallesRiesgo;
    @Autowired
    AnalisisTrabajoSeguroRepository analisisRepo;

    private void usuario(String nombre) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(nombre, null, Collections.emptyList()));
    }

    @AfterEach
    void liberarSesion() {
        SecurityContextHolder.clearContext();
    }

    private CotizacionRequestDTO form(Long solicitudId, String descripcion) {
        return new CotizacionRequestDTO(solicitudId, descripcion, "Observación",
                List.of(new DetalleCotizacionRequestDTO(descripcion, BigDecimal.valueOf(2), new BigDecimal("100.00"), "SERVICIO")));
    }

    private SolicitudServicio solicitud() {
        usuario("gerente");
        return solicitudes.registrar(new SolicitudServicioRequestDTO(
                empresas.findAll().get(0).getId(), tipos.findAll().get(0).getId(),
                "Revisión de tableros eléctricos", null, null, null));
    }

    private static MockMultipartFile pdf(String nombre) {
        return new MockMultipartFile("pdf", nombre, "application/pdf", "%PDF-1.4\n%%EOF".getBytes());
    }

    @Test
    void solicitudPermiteVariasCotizacionesConDetalleYUnSoloAceptado() {
        SolicitudServicio s = solicitud();
        Cotizacion a = cotizaciones.registrar(form(s.getId(), "Alternativa A"));
        Cotizacion b = cotizaciones.registrar(form(s.getId(), "Alternativa B"));
        assertEquals(2, cotizacionesRepo.findBySolicitudServicioId(s.getId()).size());
        assertEquals(1, detallesCotizacion.findByCotizacionIdOrderByItemAsc(a.getId()).size());
        assertEquals(new BigDecimal("236.00"), a.getTotal());
        cotizaciones.cambiarEstado(a.getId(), "ACEPTADA");
        assertThrows(NegocioException.class, () -> cotizaciones.cambiarEstado(b.getId(), "ACEPTADA"));
    }

    @Test
    void flujoCompletoHastaAnalisisTrabajoSeguroNormalizado() {
        SolicitudServicio s = solicitud();
        Cotizacion c = cotizaciones.registrar(form(s.getId(), "Mantenimiento preventivo"));
        cotizaciones.cambiarEstado(c.getId(), "ACEPTADA");
        OrdenCompra oc = compras.registrar(c.getId(), "OC-TEST-001", LocalDate.now(), c.getTotal(), pdf("oc.pdf"), null);
        assertTrue(oc.getUrlArchivo().contains("/api/v1/archivos/"));
        assertEquals(1, detallesCompra.findByOrdenCompraIdOrderByItemAsc(oc.getId()).size());
        List<Empleado> tech = empleados.findAll().stream().filter(e -> "TECNICO".equals(e.getCargo().getNombre())).toList();
        Empleado ssoma = empleados.findAll().stream().filter(e -> "SSOMA".equals(e.getCargo().getNombre())).findFirst().orElseThrow();
        usuario("supervisor");
        OrdenTrabajo ot = asignaciones.registrar(new AsignacionPersonalRequestDTO(s.getId(), tech.get(0).getId(),
                ssoma.getId(), null, List.of(tech.get(1).getId())));
        assertEquals(3, asignacionesRepo.findByOrdenTrabajoIdAndEstado(ot.getId(), "A").size());
        LocalDateTime inicio = LocalDateTime.now().plusDays(3).withSecond(0).withNano(0);
        programacion.programar(ot.getId(), inicio, inicio.plusHours(3), "Sala de tableros",
                LocalDate.now(), true, pdf("pt.pdf"), pdf("iper.pdf"), null, null);
        assertEquals(2, documentos.findByOrdenTrabajoId(ot.getId()).size());
        usuario("ssoma");
        AnalisisTrabajoSeguro a = analisis.elaborar(new AnalisisTrabajoSeguroRequestDTO(ot.getId(), "Área señalizada",
                List.of("Contacto eléctrico"), List.of("Guantes aislantes"), "Multímetro",
                List.of(new AtsEtapaDTO("Inspección", "Tablero energizado", "Electrocución", "ALTO", "Desenergizar el área"))));
        assertEquals("PENDIENTE", a.getEstado());
        assertEquals(1, detallesRiesgo.findByAnalisisTrabajoSeguroId(a.getId()).size());
        assertEquals(1, analisisRepo.count());
    }
}
