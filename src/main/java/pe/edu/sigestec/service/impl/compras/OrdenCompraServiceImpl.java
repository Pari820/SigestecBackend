package pe.edu.sigestec.service.impl.compras;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.compras.DetalleOrdenCompraRequestDTO;
import pe.edu.sigestec.entity.compras.DetalleOrdenCompra;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.compras.DetalleOrdenCompraRepository;
import pe.edu.sigestec.repository.compras.OrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.DetalleCotizacionRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.compras.OrdenCompraService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class OrdenCompraServiceImpl implements OrdenCompraService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final OrdenCompraRepository compras;
    private final DetalleOrdenCompraRepository detalles;
    private final DetalleCotizacionRepository detalleCotizacion;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService storage;
    private final CorrelativoDocumentoService correlativos;

    public OrdenCompraServiceImpl(OrdenCompraRepository c, DetalleOrdenCompraRepository d,
                                  DetalleCotizacionRepository dc, ReferenciasService r, UsuarioActualService a, ArchivoStorageService s,
                                  CorrelativoDocumentoService correlativos) {
        compras = c;
        detalles = d;
        detalleCotizacion = dc;
        ref = r;
        actual = a;
        storage = s;
        this.correlativos = correlativos;
    }

    @Override
    public OrdenCompra registrar(Long cotizacionId, String numero, LocalDate fecha, BigDecimal monto,
                                 MultipartFile pdf, List<DetalleOrdenCompraRequestDTO> lineas) {
        Cotizacion c = ref.cotizacion(cotizacionId);
        if (!"ACEPTADA".equals(c.getEstado())) throw NegocioException.conflicto("La cotización debe estar ACEPTADA");
        if (compras.existsByCotizacionId(cotizacionId)) throw NegocioException.conflicto("La cotización ya tiene OC");
        if (numero == null || numero.isBlank() || compras.existsByNumeroOrdenCompraIgnoreCase(numero.trim()))
            throw NegocioException.conflicto("Número de OC vacío o duplicado");
        if (fecha == null || monto == null || monto.signum() < 0)
            throw NegocioException.invalido("Fecha y monto válidos son obligatorios");

        String url = storage.guardar(pdf, true, true);
        OrdenCompra oc = new OrdenCompra();
        oc.setCodigoInterno(correlativos.siguiente("ORDEN_COMPRA", "OC", fecha.getYear(), 3));
        oc.setCotizacion(c);
        oc.setEmpresaCliente(c.getEmpresaCliente());
        oc.setEmpleadoRegistra(actual.empleado());
        oc.setNumeroOrdenCompra(numero.trim());
        oc.setFechaEmision(fecha);
        oc.setMontoTotal(monto);
        oc.setUrlArchivo(url);
        compras.saveAndFlush(oc);
        if (lineas == null || lineas.isEmpty()) {
            lineas = detalleCotizacion.findByCotizacionIdOrderByItemAsc(cotizacionId).stream()
                    .map(d -> new DetalleOrdenCompraRequestDTO(d.getDescripcion(), d.getCantidad(), d.getPrecioUnitario(), d.getUnidadMedida())).toList();
        }
        if (lineas.isEmpty()) throw NegocioException.invalido("La OC requiere al menos un detalle");
        int i = 1;
        for (var d : lineas) {
            if (d.descripcion() == null || d.descripcion().isBlank() || d.cantidad() == null || d.precioUnitario() == null
                    || d.cantidad().signum() <= 0 || d.precioUnitario().signum() < 0)
                throw NegocioException.invalido("Detalle de OC inválido");
            DetalleOrdenCompra det = new DetalleOrdenCompra();
            det.setOrdenCompra(oc);
            det.setItem(i++);
            det.setDescripcion(d.descripcion());
            det.setCantidad(d.cantidad());
            det.setPrecioUnitario(d.precioUnitario());
            det.setUnidadMedida(d.unidadMedida() == null ? "SERVICIO" : d.unidadMedida());
            det.setSubtotal(d.cantidad().multiply(d.precioUnitario()).setScale(2, RoundingMode.HALF_UP));
            detalles.save(det);
        }
        auditoriaEventos.publishEvent(EventoAuditoria.de("ORDEN_COMPRA_REGISTRADA", "OrdenCompra", oc.getId(), "Orden de compra registrada"));
        return oc;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.compras.OrdenCompra> listar() {
        return compras.findAllByOrderByFechaEmisionAscIdAsc();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public OrdenCompra buscarPorId(Long id) {
        return compras.findById(id)
            .orElseThrow(() -> pe.edu.sigestec.exception.NegocioException.noExiste("OC no encontrada"));
    }

}
