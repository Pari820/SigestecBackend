package pe.edu.sigestec.service.impl.common;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.entity.asignaciones.AsignacionPersonal;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
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

@Service
@Transactional(readOnly = true)
public class RespuestaComercialBuilder {
    private final VistaCamposMapper campos;
    private final DetalleCotizacionRepository detallesCotizacion;
    private final DetalleOrdenCompraRepository detallesCompra;
    private final AsignacionPersonalRepository asignaciones;
    private final DocumentoPlanificacionRepository documentos;
    private final DetalleAnalisisRiesgoRepository riesgos;
    private final AnalisisEquipoProteccionPersonalRepository protecciones;
    private final AnalisisHerramientaEquipoRepository herramientas;
    private final RepresentanteEmpresaRepository representantes;

    public RespuestaComercialBuilder(DetalleCotizacionRepository dc, DetalleOrdenCompraRepository doo,
                  AsignacionPersonalRepository a, DocumentoPlanificacionRepository dp,
                  DetalleAnalisisRiesgoRepository r, AnalisisEquipoProteccionPersonalRepository p,
                  AnalisisHerramientaEquipoRepository h, RepresentanteEmpresaRepository reps, VistaCamposMapper campos) {
        this.campos = campos;
        detallesCotizacion = dc;
        detallesCompra = doo;
        asignaciones = a;
        documentos = dp;
        riesgos = r;
        protecciones = p;
        herramientas = h;
        representantes = reps;
    }

    private static String n(String s) {
        return s == null ? "" : s;
    }

    public Map<String, Object> cliente(PersonaJuridica x) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("numeroRegistro", x.getNumeroRegistro());
        m.put("ruc", x.getRuc());
        m.put("correo", n(x.getCorreo()));
        m.put("estado", x.getEstado());
        m.put("razonSocial", x.getRazonSocial());
        m.put("telefono", n(x.getTelefono()));
        m.put("contacto", representantes.findByPersonaJuridicaId(x.getId()).stream().findFirst()
                .map(r -> r.getPersonaNatural().getNombres() + " " + r.getPersonaNatural().getApellidos()).orElse(""));
        return m;
    }

    public Map<String, Object> tipo(TipoServicio x) {
        return campos.tipo(x);
    }

    public Map<String, Object> empleado(Empleado x) {
        return campos.empleado(x);
    }

    public Map<String, Object> solicitud(SolicitudServicio x) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("codigo", x.getCodigoSolicitud());
        m.put("cliente", cliente(x.getEmpresaCliente()));
        m.put("tipoServicio", tipo(x.getTipoServicio()));
        m.put("descripcion", x.getDescripcion());
        m.put("observaciones", n(x.getObservaciones()));
        m.put("lugarServicio", n(x.getLugarServicio()));
        m.put("fechaRecepcion", x.getFechaSolicitud());
        m.put("estado", x.getEstado());
        m.put("empleadoRegistra", empleado(x.getEmpleadoRegistra()));
        m.put("representante", x.getRepresentanteSolicitante() == null ? null : x.getRepresentanteSolicitante().getId());
        m.put("urlArchivo", x.getUrlArchivoAdjunto());
        return m;
    }

    public Map<String, Object> cotizacion(Cotizacion x) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("solicitud", solicitud(x.getSolicitudServicio()));
        m.put("descripcionTrabajo", detallesCotizacion.findByCotizacionIdOrderByItemAsc(x.getId()).stream().map(DetalleCotizacion::getDescripcion).findFirst().orElse(""));
        m.put("observaciones", n(x.getObservaciones()));
        m.put("monto", x.getTotal());
        m.put("subtotal", x.getSubtotal());
        m.put("igv", x.getIgv());
        m.put("numero", x.getNumeroCotizacion());
        m.put("estado", x.getEstado());
        m.put("fecha", x.getFechaEmision());
        m.put("urlArchivo", x.getUrlArchivo());
        m.put("empleadoRegistra", empleado(x.getEmpleadoRegistra()));
        m.put("detalles", detallesCotizacion.findByCotizacionIdOrderByItemAsc(x.getId()).stream().map(d -> Map.of(
                "id", d.getId(), "item", d.getItem(), "descripcion", d.getDescripcion(), "cantidad", d.getCantidad(),
                "precioUnitario", d.getPrecioUnitario(), "subtotal", d.getSubtotal(), "unidadMedida", n(d.getUnidadMedida()))).toList());
        return m;
    }

    public Map<String, Object> compra(OrdenCompra x) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("cotizacion", cotizacion(x.getCotizacion()));
        m.put("codigo", x.getCodigoInterno());
        m.put("numero", x.getNumeroOrdenCompra());
        m.put("estado", x.getEstado());
        m.put("fechaEmision", x.getFechaEmision());
        m.put("montoTotal", x.getMontoTotal());
        m.put("urlArchivo", x.getUrlArchivo());
        m.put("nombrePdf", x.getNumeroOrdenCompra() + ".pdf");
        m.put("empleadoRegistra", empleado(x.getEmpleadoRegistra()));
        m.put("detalles", detallesCompra.findByOrdenCompraIdOrderByItemAsc(x.getId()).stream().map(d -> Map.of(
                "id", d.getId(), "item", d.getItem(), "descripcion", d.getDescripcion(), "cantidad", d.getCantidad(),
                "unidadMedida", n(d.getUnidadMedida()), "precioUnitario", d.getPrecioUnitario() == null ? BigDecimal.ZERO : d.getPrecioUnitario(),
                "subtotal", d.getSubtotal() == null ? BigDecimal.ZERO : d.getSubtotal())).toList());
        return m;
    }

    public Map<String, Object> orden(OrdenTrabajo x) {
        List<AsignacionPersonal> as = asignaciones.findByOrdenTrabajoIdAndEstado(x.getId(), "A");
        java.util.function.Function<String, Object> porCargo = cargo -> as.stream().filter(a -> cargo.equals(a.getFuncionServicio()))
                .map(a -> (Object) empleado(a.getEmpleado())).findFirst().orElse(null);
        List<DocumentoPlanificacion> docs = documentos.findByOrdenTrabajoId(x.getId());
        java.util.function.Function<String, String> nombre = tipo -> docs.stream().filter(d -> tipo.equals(d.getTipoDocumento()))
                .map(DocumentoPlanificacion::getNombreDocumento).findFirst().orElse("");
        LocalDateTimeWrapper helper = new LocalDateTimeWrapper(docs);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("solicitud", solicitud(x.getOrdenCompra().getCotizacion().getSolicitudServicio()));
        m.put("tecnico", porCargo.apply("TECNICO"));
        m.put("ssoma", porCargo.apply("SSOMA"));
        m.put("apoyo", porCargo.apply("APOYO"));
        m.put("asignaciones", as.stream().map(a -> Map.of("id", a.getId(), "empleado", empleado(a.getEmpleado()), "funcion", a.getFuncionServicio())).toList());
        m.put("tecnicos", as.stream().filter(a -> "TECNICO".equals(a.getFuncionServicio())).map(a -> empleado(a.getEmpleado())).toList());
        m.put("numero", x.getNumeroOrdenTrabajo());
        m.put("estado", x.getEstado());
        m.put("inicio", x.getFechaProgramadaInicio());
        m.put("fin", x.getFechaProgramadaFin());
        m.put("lugar", n(x.getLugarTrabajo()));
        m.put("fechaEnvio", helper.fecha());
        m.put("aprobadoCliente", docs.stream().anyMatch(d -> "APROBADO".equals(d.getEstado())));
        m.put("motivoCambio", docs.stream().map(DocumentoPlanificacion::getObservaciones).filter(s -> s != null && !s.isBlank()).findFirst().orElse(""));
        m.put("nombrePt", nombre.apply("PROCEDIMIENTO_TRABAJO"));
        m.put("nombreIper", nombre.apply("IPER"));
        m.put("nombreCroquis", nombre.apply("CROQUIS"));
        m.put("empleadoRegistra", empleado(x.getEmpleadoRegistra()));
        return m;
    }

    private record LocalDateTimeWrapper(List<DocumentoPlanificacion> docs) {
        Object fecha() {
            return docs.stream().map(DocumentoPlanificacion::getFechaEnvioCliente)
                    .filter(Objects::nonNull).map(java.time.LocalDateTime::toLocalDate).findFirst().orElse(null);
        }
    }

    public Map<String, Object> analisisTrabajoSeguro(AnalisisTrabajoSeguro x) {
        var etapas = riesgos.findByAnalisisTrabajoSeguroId(x.getId()).stream().map(d -> Map.of(
                "etapa", n(d.getEtapaTrabajo()), "peligro", n(d.getPeligro()), "riesgo", d.getRiesgo().getNombre(),
                "nivel", n(d.getNivelRiesgo()), "medidaControl", n(d.getMedidaControl()))).toList();
        var epp = protecciones.findByAnalisisTrabajoSeguroId(x.getId()).stream()
                .map(a -> a.getEquipoProteccionPersonal().getNombre()).toList();
        var hers = herramientas.findByAnalisisTrabajoSeguroId(x.getId()).stream()
                .map(a -> a.getHerramientaEquipo().getNombre()).toList();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", x.getId());
        m.put("numero", x.getNumeroAts());
        m.put("ordenTrabajo", orden(x.getOrdenTrabajo()));
        m.put("condicionesArea", x.getCondicionesArea());
        m.put("peligros", String.join(" | ", etapas.stream().map(s -> s.get("peligro")).distinct().toList()));
        m.put("epp", String.join(" | ", epp));
        m.put("herramientas", String.join(" | ", hers));
        m.put("etapas", etapas);
        m.put("estado", x.getEstado());
        m.put("urlArchivo", x.getUrlArchivo());
        m.put("observaciones", n(x.getObservaciones()));
        m.put("fechaElaboracion", x.getFechaRegistro());
        m.put("empleadoElabora", empleado(x.getEmpleadoElabora()));
        return m;
    }
}
