package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.compras.OrdenCompraRepository;
import pe.edu.sigestec.repository.cotizaciones.CotizacionRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.repository.solicitudes.SolicitudServicioRepository;

class ReferenciasServiceTest {
    private final SolicitudServicioRepository solicitudes = mock(SolicitudServicioRepository.class);
    private final CotizacionRepository cotizaciones = mock(CotizacionRepository.class);
    private final OrdenCompraRepository compras = mock(OrdenCompraRepository.class);
    private final OrdenTrabajoRepository ordenes = mock(OrdenTrabajoRepository.class);
    private final EmpleadoRepository empleados = mock(EmpleadoRepository.class);
    private final ReferenciasService service =
            new ReferenciasService(solicitudes, cotizaciones, compras, ordenes, empleados);

    @Test
    void referenciasInexistentesProducenErrorDeNegocio() {
        when(solicitudes.findById(1L)).thenReturn(Optional.empty());
        when(cotizaciones.findById(1L)).thenReturn(Optional.empty());
        when(compras.findById(1L)).thenReturn(Optional.empty());
        when(ordenes.findById(1L)).thenReturn(Optional.empty());
        when(empleados.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NegocioException.class, () -> service.solicitud(1L));
        assertThrows(NegocioException.class, () -> service.cotizacion(1L));
        assertThrows(NegocioException.class, () -> service.compra(1L));
        assertThrows(NegocioException.class, () -> service.orden(1L));
        assertThrows(NegocioException.class, () -> service.empleado(1L, "SUPERVISOR"));
    }

    @Test
    void empleadoInactivoNoEsReferenciaValida() {
        Empleado empleado = mock(Empleado.class);
        when(empleados.findById(2L)).thenReturn(Optional.of(empleado));
        when(empleado.getEstado()).thenReturn("I");

        assertThrows(NegocioException.class, () -> service.empleado(2L, "SUPERVISOR"));
        verify(empleado, never()).getCargo();
    }

    @Test
    void empleadoConCargoDiferenteNoEsReferenciaValida() {
        Empleado empleado = mock(Empleado.class);
        var cargo = mock(pe.edu.sigestec.entity.personal.Cargo.class);
        when(empleados.findById(3L)).thenReturn(Optional.of(empleado));
        when(empleado.getEstado()).thenReturn("A");
        when(empleado.getCargo()).thenReturn(cargo);
        when(cargo.getNombre()).thenReturn("OPERARIO");

        assertThrows(NegocioException.class, () -> service.empleado(3L, "SUPERVISOR"));
    }
}
