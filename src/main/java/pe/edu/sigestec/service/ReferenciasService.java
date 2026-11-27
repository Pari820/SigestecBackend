package pe.edu.sigestec.service;

import org.springframework.stereotype.Service;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.compras.OrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.CotizacionRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.repository.solicitudes.SolicitudServicioRepository;

@Service
public class ReferenciasService {
    private final SolicitudServicioRepository solicitudes;
    private final CotizacionRepository cotizaciones;
    private final OrdenCompraRepository compras;
    private final OrdenTrabajoRepository ordenes;
    private final EmpleadoRepository empleados;

    public ReferenciasService(SolicitudServicioRepository s, CotizacionRepository c, OrdenCompraRepository oc,
                              OrdenTrabajoRepository ot, EmpleadoRepository e) {
        solicitudes = s;
        cotizaciones = c;
        compras = oc;
        ordenes = ot;
        empleados = e;
    }

    public SolicitudServicio solicitud(Long id) {
        return solicitudes.findById(id).orElseThrow(() -> NegocioException.noExiste("Solicitud no encontrada"));
    }

    public Cotizacion cotizacion(Long id) {
        return cotizaciones.findById(id).orElseThrow(() -> NegocioException.noExiste("Cotización no encontrada"));
    }

    public OrdenCompra compra(Long id) {
        return compras.findById(id).orElseThrow(() -> NegocioException.noExiste("Orden de compra no encontrada"));
    }

    public OrdenTrabajo orden(Long id) {
        return ordenes.findById(id).orElseThrow(() -> NegocioException.noExiste("Orden de trabajo no encontrada"));
    }

    public Empleado empleado(Long id, String cargo) {
        Empleado e = empleados.findById(id).orElseThrow(() -> NegocioException.noExiste("Empleado no encontrado"));
        if (!"A".equals(e.getEstado()) || !cargo.equals(e.getCargo().getNombre()))
            throw NegocioException.invalido("El empleado debe estar activo y ser " + cargo);
        return e;
    }
}
