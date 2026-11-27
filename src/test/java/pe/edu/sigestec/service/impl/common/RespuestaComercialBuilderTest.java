package pe.edu.sigestec.service.impl.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.asignaciones.AsignacionPersonal;
import pe.edu.sigestec.entity.ats.AnalisisEquipoProteccionPersonal;
import pe.edu.sigestec.entity.ats.AnalisisHerramientaEquipo;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.DetalleAnalisisRiesgo;
import pe.edu.sigestec.entity.ats.EquipoProteccionPersonal;
import pe.edu.sigestec.entity.ats.Riesgo;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.compras.DetalleOrdenCompra;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.cotizaciones.DetalleCotizacion;
import pe.edu.sigestec.entity.orden_trabajo.DocumentoPlanificacion;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.mapper.common.VistaCamposMapper;
import pe.edu.sigestec.repository.asignaciones.AsignacionPersonalRepository;
import pe.edu.sigestec.repository.ats.AnalisisEquipoProteccionPersonalRepository;
import pe.edu.sigestec.repository.ats.AnalisisHerramientaEquipoRepository;
import pe.edu.sigestec.repository.ats.DetalleAnalisisRiesgoRepository;
import pe.edu.sigestec.repository.catalogos.RepresentanteEmpresaRepository;
import pe.edu.sigestec.repository.compras.DetalleOrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.DetalleCotizacionRepository;
import pe.edu.sigestec.repository.orden_trabajo.DocumentoPlanificacionRepository;

@ExtendWith(MockitoExtension.class)
class RespuestaComercialBuilderTest {

    @Mock private DetalleCotizacionRepository detallesCotizacion;
    @Mock private DetalleOrdenCompraRepository detallesCompra;
    @Mock private AsignacionPersonalRepository asignaciones;
    @Mock private DocumentoPlanificacionRepository documentos;
    @Mock private DetalleAnalisisRiesgoRepository riesgos;
    @Mock private AnalisisEquipoProteccionPersonalRepository protecciones;
    @Mock private AnalisisHerramientaEquipoRepository herramientas;
    @Mock private RepresentanteEmpresaRepository representantes;
    @Mock private VistaCamposMapper campos;

    private RespuestaComercialBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new RespuestaComercialBuilder(
            detallesCotizacion, detallesCompra, asignaciones, documentos,
            riesgos, protecciones, herramientas, representantes, campos);
    }

    private SolicitudServicio solicitudBase() {
        SolicitudServicio solicitud = mock(SolicitudServicio.class);
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        TipoServicio tipo = mock(TipoServicio.class);
        Empleado empleado = mock(Empleado.class);

        when(cliente.getId()).thenReturn(10L);
        when(representantes.findByPersonaJuridicaId(10L)).thenReturn(List.of());
        when(solicitud.getEmpresaCliente()).thenReturn(cliente);
        when(solicitud.getTipoServicio()).thenReturn(tipo);
        when(solicitud.getEmpleadoRegistra()).thenReturn(empleado);
        when(campos.tipo(tipo)).thenReturn(Map.of("nombre", "Servicio"));
        when(campos.empleado(empleado)).thenReturn(Map.of("nombre", "Empleado"));
        return solicitud;
    }

    @Test
    void clienteDevuelveDatosYValoresVacios() {
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        when(cliente.getId()).thenReturn(10L);
        when(representantes.findByPersonaJuridicaId(10L)).thenReturn(List.of());

        Map<String, Object> resultado = builder.cliente(cliente);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("id"));
        assertTrue(resultado.containsKey("ruc"));
        assertTrue(resultado.containsKey("razonSocial"));
        assertEquals("", resultado.get("correo"));
        assertEquals("", resultado.get("telefono"));
        assertEquals("", resultado.get("contacto"));
        verify(representantes).findByPersonaJuridicaId(10L);
    }

    @Test
    void clienteIncluyeNombreDelRepresentanteCuandoExiste() {
        PersonaJuridica cliente = mock(PersonaJuridica.class);
        RepresentanteEmpresa representante = mock(RepresentanteEmpresa.class);
        PersonaNatural persona = mock(PersonaNatural.class);

        when(cliente.getId()).thenReturn(11L);
        when(representantes.findByPersonaJuridicaId(11L)).thenReturn(List.of(representante));
        when(representante.getPersonaNatural()).thenReturn(persona);
        when(persona.getNombres()).thenReturn("Ana");
        when(persona.getApellidos()).thenReturn("Pérez");

        Map<String, Object> resultado = builder.cliente(cliente);

        assertEquals("Ana Pérez", resultado.get("contacto"));
    }

    @Test
    void tipoDelegaEnMapper() {
        TipoServicio tipo = mock(TipoServicio.class);
        Map<String, Object> esperado = Map.of("nombre", "Mantenimiento");
        when(campos.tipo(tipo)).thenReturn(esperado);

        assertEquals(esperado, builder.tipo(tipo));
        verify(campos).tipo(tipo);
    }

    @Test
    void empleadoDelegaEnMapper() {
        Empleado empleado = mock(Empleado.class);
        Map<String, Object> esperado = Map.of("nombre", "Trabajador");
        when(campos.empleado(empleado)).thenReturn(esperado);

        assertEquals(esperado, builder.empleado(empleado));
        verify(campos).empleado(empleado);
    }

    @Test
    void solicitudConstruyeRespuestaCorrectamente() {
        SolicitudServicio solicitud = solicitudBase();

        Map<String, Object> resultado = builder.solicitud(solicitud);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("id"));
        assertTrue(resultado.containsKey("codigo"));
        assertTrue(resultado.containsKey("cliente"));
        assertTrue(resultado.containsKey("tipoServicio"));
        assertTrue(resultado.containsKey("empleadoRegistra"));
        assertEquals("", resultado.get("observaciones"));
        assertEquals("", resultado.get("lugarServicio"));
        assertNull(resultado.get("representante"));
    }

    @Test
    void solicitudIncluyeRepresentanteCuandoEstaInformado() {
        SolicitudServicio solicitud = solicitudBase();
        RepresentanteEmpresa representante = mock(RepresentanteEmpresa.class);
        when(representante.getId()).thenReturn(77L);
        when(solicitud.getRepresentanteSolicitante()).thenReturn(representante);

        Map<String, Object> resultado = builder.solicitud(solicitud);

        assertEquals(77L, resultado.get("representante"));
    }

    @Test
    void cotizacionConstruyeRespuestaSinDetalles() {
        Cotizacion cotizacion = mock(Cotizacion.class);
        when(cotizacion.getId()).thenReturn(20L);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(detallesCotizacion.findByCotizacionIdOrderByItemAsc(20L)).thenReturn(List.of());

        Map<String, Object> resultado = builder.cotizacion(cotizacion);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("solicitud"));
        assertTrue(resultado.containsKey("descripcionTrabajo"));
        assertTrue(resultado.containsKey("detalles"));
        assertEquals("", resultado.get("descripcionTrabajo"));
        assertTrue(((List<?>) resultado.get("detalles")).isEmpty());
        verify(detallesCotizacion, times(2)).findByCotizacionIdOrderByItemAsc(20L);
    }

    @Test
    void cotizacionIncluyeDescripcionYDetalle() {
        Cotizacion cotizacion = mock(Cotizacion.class);
        DetalleCotizacion detalle = mock(DetalleCotizacion.class);
        when(cotizacion.getId()).thenReturn(21L);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(detalle.getId()).thenReturn(1L);
        when(detalle.getItem()).thenReturn(1);
        when(detalle.getDescripcion()).thenReturn("Mantenimiento preventivo");
        when(detalle.getCantidad()).thenReturn(BigDecimal.valueOf(2));
        when(detalle.getPrecioUnitario()).thenReturn(BigDecimal.TEN);
        when(detalle.getSubtotal()).thenReturn(BigDecimal.valueOf(20));
        when(detalle.getUnidadMedida()).thenReturn(null);
        when(detallesCotizacion.findByCotizacionIdOrderByItemAsc(21L))
            .thenReturn(List.of(detalle));

        Map<String, Object> resultado = builder.cotizacion(cotizacion);

        assertEquals("Mantenimiento preventivo", resultado.get("descripcionTrabajo"));
        List<?> items = (List<?>) resultado.get("detalles");
        assertEquals(1, items.size());
        assertEquals("", ((Map<?, ?>) items.get(0)).get("unidadMedida"));
    }

    @Test
    void compraConstruyeRespuestaSinDetalles() {
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        when(compra.getId()).thenReturn(30L);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        when(cotizacion.getId()).thenReturn(20L);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(detallesCotizacion.findByCotizacionIdOrderByItemAsc(20L)).thenReturn(List.of());
        when(detallesCompra.findByOrdenCompraIdOrderByItemAsc(30L)).thenReturn(List.of());

        Map<String, Object> resultado = builder.compra(compra);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("cotizacion"));
        assertTrue(resultado.containsKey("codigo"));
        assertTrue(resultado.containsKey("numero"));
        assertTrue(resultado.containsKey("detalles"));
        assertTrue(((List<?>) resultado.get("detalles")).isEmpty());
    }

    @Test
    void compraConvierteImportesNulosEnCero() {
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        DetalleOrdenCompra detalle = mock(DetalleOrdenCompra.class);
        when(compra.getId()).thenReturn(31L);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        when(cotizacion.getId()).thenReturn(22L);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(detallesCotizacion.findByCotizacionIdOrderByItemAsc(22L)).thenReturn(List.of());
        when(detallesCompra.findByOrdenCompraIdOrderByItemAsc(31L)).thenReturn(List.of(detalle));
        when(detalle.getId()).thenReturn(2L);
        when(detalle.getItem()).thenReturn(1);
        when(detalle.getDescripcion()).thenReturn("Repuesto");
        when(detalle.getCantidad()).thenReturn(BigDecimal.ONE);
        when(detalle.getUnidadMedida()).thenReturn(null);
        when(detalle.getPrecioUnitario()).thenReturn(null);
        when(detalle.getSubtotal()).thenReturn(null);

        Map<String, Object> resultado = builder.compra(compra);
        Map<?, ?> item = (Map<?, ?>) ((List<?>) resultado.get("detalles")).get(0);

        assertEquals(BigDecimal.ZERO, item.get("precioUnitario"));
        assertEquals(BigDecimal.ZERO, item.get("subtotal"));
        assertEquals("", item.get("unidadMedida"));
    }

    @Test
    void ordenConstruyeRespuestaSinAsignacionesNiDocumentos() {
        OrdenTrabajo orden = mock(OrdenTrabajo.class);
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        when(orden.getId()).thenReturn(40L);
        when(orden.getOrdenCompra()).thenReturn(compra);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(asignaciones.findByOrdenTrabajoIdAndEstado(40L, "A")).thenReturn(List.of());
        when(documentos.findByOrdenTrabajoId(40L)).thenReturn(List.of());

        Map<String, Object> resultado = builder.orden(orden);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("solicitud"));
        assertNull(resultado.get("tecnico"));
        assertNull(resultado.get("ssoma"));
        assertNull(resultado.get("apoyo"));
        assertTrue(((List<?>) resultado.get("asignaciones")).isEmpty());
        assertTrue(((List<?>) resultado.get("tecnicos")).isEmpty());
        assertEquals(false, resultado.get("aprobadoCliente"));
        assertEquals("", resultado.get("nombrePt"));
        assertEquals("", resultado.get("nombreIper"));
        assertEquals("", resultado.get("nombreCroquis"));
    }

    @Test
    void ordenIncluyeAsignacionesDocumentosYFechaDeEnvio() {
        OrdenTrabajo orden = mock(OrdenTrabajo.class);
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        AsignacionPersonal asignacion = mock(AsignacionPersonal.class);
        DocumentoPlanificacion documento = mock(DocumentoPlanificacion.class);
        Empleado empleado = mock(Empleado.class);
        LocalDateTime fechaEnvio = LocalDateTime.of(2026, 5, 10, 9, 30);

        when(orden.getId()).thenReturn(41L);
        when(orden.getOrdenCompra()).thenReturn(compra);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(asignaciones.findByOrdenTrabajoIdAndEstado(41L, "A")).thenReturn(List.of(asignacion));
        when(asignacion.getFuncionServicio()).thenReturn("TECNICO");
        when(asignacion.getId()).thenReturn(8L);
        when(asignacion.getEmpleado()).thenReturn(empleado);
        when(campos.empleado(empleado)).thenReturn(Map.of("nombre", "Técnico"));
        when(documentos.findByOrdenTrabajoId(41L)).thenReturn(List.of(documento));
        when(documento.getTipoDocumento()).thenReturn("PROCEDIMIENTO_TRABAJO");
        when(documento.getNombreDocumento()).thenReturn("procedimiento.pdf");
        when(documento.getEstado()).thenReturn("APROBADO");
        when(documento.getObservaciones()).thenReturn("Cambio autorizado");
        when(documento.getFechaEnvioCliente()).thenReturn(fechaEnvio);

        Map<String, Object> resultado = builder.orden(orden);

        assertNotNull(resultado.get("tecnico"));
        assertEquals(1, ((List<?>) resultado.get("asignaciones")).size());
        assertEquals(1, ((List<?>) resultado.get("tecnicos")).size());
        assertEquals(true, resultado.get("aprobadoCliente"));
        assertEquals("procedimiento.pdf", resultado.get("nombrePt"));
        assertEquals("Cambio autorizado", resultado.get("motivoCambio"));
        assertEquals(fechaEnvio.toLocalDate(), resultado.get("fechaEnvio"));
    }

    @Test
    void analisisTrabajoSeguroConstruyeRespuestaSinDetalles() {
        AnalisisTrabajoSeguro ats = mock(AnalisisTrabajoSeguro.class);
        OrdenTrabajo orden = mock(OrdenTrabajo.class);
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        when(ats.getId()).thenReturn(50L);
        when(ats.getOrdenTrabajo()).thenReturn(orden);
        when(orden.getId()).thenReturn(40L);
        when(orden.getOrdenCompra()).thenReturn(compra);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(asignaciones.findByOrdenTrabajoIdAndEstado(40L, "A")).thenReturn(List.of());
        when(documentos.findByOrdenTrabajoId(40L)).thenReturn(List.of());
        when(riesgos.findByAnalisisTrabajoSeguroId(50L)).thenReturn(List.of());
        when(protecciones.findByAnalisisTrabajoSeguroId(50L)).thenReturn(List.of());
        when(herramientas.findByAnalisisTrabajoSeguroId(50L)).thenReturn(List.of());

        Map<String, Object> resultado = builder.analisisTrabajoSeguro(ats);

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("ordenTrabajo"));
        assertEquals("", resultado.get("peligros"));
        assertEquals("", resultado.get("epp"));
        assertEquals("", resultado.get("herramientas"));
        assertTrue(((List<?>) resultado.get("etapas")).isEmpty());
        assertEquals("", resultado.get("observaciones"));
    }

    @Test
    void analisisTrabajoSeguroIncluyeRiesgosEppYHerramientas() {
        AnalisisTrabajoSeguro ats = mock(AnalisisTrabajoSeguro.class);
        OrdenTrabajo orden = mock(OrdenTrabajo.class);
        OrdenCompra compra = mock(OrdenCompra.class);
        Cotizacion cotizacion = mock(Cotizacion.class);
        DetalleAnalisisRiesgo detalleRiesgo = mock(DetalleAnalisisRiesgo.class);
        Riesgo riesgo = mock(Riesgo.class);
        AnalisisEquipoProteccionPersonal registroEpp = mock(AnalisisEquipoProteccionPersonal.class);
        EquipoProteccionPersonal epp = mock(EquipoProteccionPersonal.class);
        AnalisisHerramientaEquipo registroHerramienta = mock(AnalisisHerramientaEquipo.class);
        HerramientaEquipo herramienta = mock(HerramientaEquipo.class);

        when(ats.getId()).thenReturn(51L);
        when(ats.getOrdenTrabajo()).thenReturn(orden);
        when(orden.getId()).thenReturn(42L);
        when(orden.getOrdenCompra()).thenReturn(compra);
        when(compra.getCotizacion()).thenReturn(cotizacion);
        SolicitudServicio solicitud = solicitudBase();
        when(cotizacion.getSolicitudServicio()).thenReturn(solicitud);
        when(asignaciones.findByOrdenTrabajoIdAndEstado(42L, "A")).thenReturn(List.of());
        when(documentos.findByOrdenTrabajoId(42L)).thenReturn(List.of());

        when(riesgos.findByAnalisisTrabajoSeguroId(51L)).thenReturn(List.of(detalleRiesgo));
        when(detalleRiesgo.getEtapaTrabajo()).thenReturn("Preparación");
        when(detalleRiesgo.getPeligro()).thenReturn("Caída");
        when(detalleRiesgo.getRiesgo()).thenReturn(riesgo);
        when(riesgo.getNombre()).thenReturn("Lesión");
        when(detalleRiesgo.getNivelRiesgo()).thenReturn("ALTO");
        when(detalleRiesgo.getMedidaControl()).thenReturn("Señalizar");

        when(protecciones.findByAnalisisTrabajoSeguroId(51L)).thenReturn(List.of(registroEpp));
        when(registroEpp.getEquipoProteccionPersonal()).thenReturn(epp);
        when(epp.getNombre()).thenReturn("Casco");

        when(herramientas.findByAnalisisTrabajoSeguroId(51L)).thenReturn(List.of(registroHerramienta));
        when(registroHerramienta.getHerramientaEquipo()).thenReturn(herramienta);
        when(herramienta.getNombre()).thenReturn("Taladro");

        Map<String, Object> resultado = builder.analisisTrabajoSeguro(ats);

        assertEquals("Caída", resultado.get("peligros"));
        assertEquals("Casco", resultado.get("epp"));
        assertEquals("Taladro", resultado.get("herramientas"));
        assertEquals(1, ((List<?>) resultado.get("etapas")).size());
    }
}
