package pe.edu.sigestec.service.impl.cotizaciones;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.cotizaciones.CotizacionRequestDTO;
import pe.edu.sigestec.dto.cotizaciones.DetalleCotizacionRequestDTO;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.cotizaciones.DetalleCotizacion;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.compras.OrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.CotizacionRepository;
import pe.edu.sigestec.repository.cotizaciones.DetalleCotizacionRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.cotizaciones.CotizacionService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class CotizacionServiceImpl implements CotizacionService {
    private final CotizacionRepository cotizaciones;
    private final DetalleCotizacionRepository detalles;
    private final OrdenCompraRepository compras;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService storage;
    private final CorrelativoDocumentoService correlativos;

    public CotizacionServiceImpl(CotizacionRepository c, DetalleCotizacionRepository d, OrdenCompraRepository oc,
                                 ReferenciasService r, UsuarioActualService a, ArchivoStorageService storage,
                                 CorrelativoDocumentoService correlativos) {
        cotizaciones = c;
        detalles = d;
        compras = oc;
        ref = r;
        actual = a;
        this.storage = storage;
        this.correlativos = correlativos;
    }

    private void validarDetalles(CotizacionRequestDTO f) {
        if (f.detalles() == null || f.detalles().isEmpty())
            throw NegocioException.invalido("La cotización requiere detalle de servicios");
        for (var d : f.detalles())
            if (d.descripcion() == null || d.descripcion().isBlank() || d.cantidad() == null || d.precioUnitario() == null ||
                    d.cantidad().signum() <= 0 || d.precioUnitario().signum() < 0)
                throw NegocioException.invalido("Detalle de cotización inválido");
    }

    private void actualizarDetalles(Cotizacion c, List<DetalleCotizacionRequestDTO> datos) {

        detalles.deleteByCotizacionId(c.getId());
        detalles.flush();
        BigDecimal subtotal = BigDecimal.ZERO;
        int i = 1;
        for (var x : datos) {
            DetalleCotizacion d = new DetalleCotizacion();
            d.setCotizacion(c);
            d.setItem(i++);
            d.setDescripcion(x.descripcion().trim());
            d.setCantidad(x.cantidad());
            d.setPrecioUnitario(x.precioUnitario());
            d.setUnidadMedida(x.unidadMedida() == null || x.unidadMedida().isBlank() ? "SERVICIO" : x.unidadMedida());
            BigDecimal importe = x.cantidad().multiply(x.precioUnitario()).setScale(2, RoundingMode.HALF_UP);
            d.setSubtotal(importe);
            subtotal = subtotal.add(importe);
            detalles.save(d);
        }
        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        c.setSubtotal(subtotal);
        c.setIgv(igv);
        c.setTotal(subtotal.add(igv));
    }

    @Override
    public Cotizacion registrar(CotizacionRequestDTO f, MultipartFile pdf) {
        validarDetalles(f);
        SolicitudServicio s = ref.solicitud(f.solicitudId());
        if (!Set.of("PENDIENTE", "COTIZADA").contains(s.getEstado()))
            throw NegocioException.conflicto("La solicitud no admite nuevas cotizaciones");
        Cotizacion c = new Cotizacion();
        c.setSolicitudServicio(s);
        c.setEmpresaCliente(s.getEmpresaCliente());
        c.setEmpleadoRegistra(actual.empleado());
        c.setNumeroCotizacion(correlativos.siguiente("COTIZACION", "COT", c.getFechaEmision().getYear(), 3));
        c.setObservaciones(f.observaciones());
        c.setUrlArchivo(storage.guardar(pdf, false, true));
        c.setEstado("ENVIADA");
        cotizaciones.saveAndFlush(c);
        actualizarDetalles(c, f.detalles());
        s.setEstado("COTIZADA");
        return cotizaciones.save(c);
    }

    @Override
    public Cotizacion editar(Long id, CotizacionRequestDTO f, MultipartFile pdf, boolean quitarPdf) {
        validarDetalles(f);
        Cotizacion c = ref.cotizacion(id);
        if (!c.getSolicitudServicio().getId().equals(f.solicitudId()))
            throw NegocioException.invalido("No cambie la solicitud de la cotización");
        if (!Set.of("BORRADOR", "ENVIADA").contains(c.getEstado()) || compras.existsByCotizacionId(id))
            throw NegocioException.conflicto("No se puede editar una cotización que ya tiene respuesta o OC");
        c.setObservaciones(f.observaciones());
        if (quitarPdf && pdf != null && !pdf.isEmpty()) throw NegocioException.invalido("No se puede quitar y reemplazar el mismo PDF");
        if (quitarPdf) c.setUrlArchivo(null);
        else if (pdf != null && !pdf.isEmpty()) c.setUrlArchivo(storage.guardar(pdf, true, true));
        actualizarDetalles(c, f.detalles());
        return cotizaciones.save(c);
    }

    @Override
    public Cotizacion cambiarEstado(Long id, String estado) {
        Cotizacion c = ref.cotizacion(id);
        String nuevo = estado.toUpperCase(Locale.ROOT);
        if (!pe.edu.sigestec.enums.DecisionCotizacion.permite(nuevo)) throw NegocioException.invalido("Estado inválido");
        if (compras.existsByCotizacionId(id) || !Set.of("BORRADOR", "ENVIADA").contains(c.getEstado()))
            throw NegocioException.conflicto("La cotización ya fue respondida o tiene una OC");
        if ("ACEPTADA".equals(nuevo) && cotizaciones.findBySolicitudServicioId(c.getSolicitudServicio().getId())
                .stream().anyMatch(otra -> !otra.getId().equals(c.getId()) && "ACEPTADA".equals(otra.getEstado())))
            throw NegocioException.conflicto("La solicitud ya tiene otra cotización aceptada");
        c.setEstado(nuevo);
        c.setFechaRespuesta(LocalDate.now());
        if ("ACEPTADA".equals(nuevo)) c.getSolicitudServicio().setEstado("ACEPTADA");
        return cotizaciones.save(c);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.cotizaciones.Cotizacion> listar() {
        return cotizaciones.findAllByOrderByFechaEmisionAscIdAsc();
    }

}
