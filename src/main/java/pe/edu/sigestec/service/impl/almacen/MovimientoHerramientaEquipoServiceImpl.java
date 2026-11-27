package pe.edu.sigestec.service.impl.almacen;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.almacen.HerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.almacen.HerramientaEquipoRepository;
import pe.edu.sigestec.repository.almacen.MovimientoHerramientaEquipoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.almacen.MovimientoHerramientaEquipoService;

@Service
@Transactional
public class MovimientoHerramientaEquipoServiceImpl implements MovimientoHerramientaEquipoService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final CorrelativoDocumentoService correlativos;
    private final HerramientaEquipoRepository herramientas;
    private final MovimientoHerramientaEquipoRepository movimientos;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    public MovimientoHerramientaEquipoServiceImpl(HerramientaEquipoRepository h, MovimientoHerramientaEquipoRepository m,
                                                  ReferenciasService r, UsuarioActualService u, CorrelativoDocumentoService correlativos){
        herramientas=h;movimientos=m;ref=r;actual=u;this.correlativos=correlativos;
    }
    @Override
    public HerramientaEquipo crear(HerramientaRequestDTO f){
        if(herramientas.findByNombreIgnoreCase(f.nombre().trim()).isPresent())throw NegocioException.conflicto("Herramienta ya registrada");
        HerramientaEquipo h=new HerramientaEquipo();
        h.setCodigoHerramienta(correlativos.siguienteSinAnio("HERRAMIENTA", "HER", 6));
        h.setNombre(f.nombre().trim());h.setDescripcion(f.descripcion());
        h.setCantidadTotal(f.cantidadInicial());h.setCantidadDisponible(f.cantidadInicial());h.setEstado("A");
        HerramientaEquipo resultado = herramientas.saveAndFlush(h);
        auditoriaEventos.publishEvent(EventoAuditoria.de("HERRAMIENTA_REGISTRADA", "HerramientaEquipo", resultado.getId(), "Herramienta registrada en catálogo"));
        return resultado;
    }
    @Override
    public MovimientoHerramientaEquipo registrar(MovimientoHerramientaRequestDTO f){
        HerramientaEquipo h=herramientas.buscarParaActualizar(f.herramientaId()).orElseThrow(()->NegocioException.noExiste("Herramienta no encontrada"));
        if(!"A".equals(h.getEstado()))throw NegocioException.conflicto("Herramienta inactiva");
        String t=f.tipoMovimiento();
        OrdenTrabajo ot=null;Empleado responsable=null;
        if(!"INGRESO".equals(t)){
            if(f.ordenTrabajoId()==null||f.empleadoResponsableId()==null)
                throw NegocioException.invalido("Indica la OT y el empleado responsable");
            ot=ref.orden(f.ordenTrabajoId());
            if(!Set.of("PROGRAMADA","EJECUTADA","ATENDIDA").contains(ot.getEstado()))throw NegocioException.conflicto("OT no habilitada");
            responsable=ref.empleado(f.empleadoResponsableId(),"TECNICO");
        }
        BigDecimal cantidad=f.cantidad();
        if("PRESTAMO".equals(t)){
            if(h.getCantidadDisponible().compareTo(cantidad)<0)throw NegocioException.conflicto("No hay herramientas suficientes disponibles");
            h.setCantidadDisponible(h.getCantidadDisponible().subtract(cantidad));
        } else if("DEVOLUCION".equals(t)) {
            BigDecimal saldo=BigDecimal.ZERO;
            for(MovimientoHerramientaEquipo mov:movimientos.findByOrdenTrabajoIdAndHerramientaEquipoIdAndEmpleadoResponsableId(ot.getId(),h.getId(),responsable.getId())){
                if("PRESTAMO".equals(mov.getTipoMovimiento()))saldo=saldo.add(mov.getCantidad());
                if("DEVOLUCION".equals(mov.getTipoMovimiento()))saldo=saldo.subtract(mov.getCantidad());
            }
            if(saldo.compareTo(cantidad)<0)throw NegocioException.conflicto("La devolución excede el préstamo pendiente de este empleado y OT");
            if(f.estadoDevolucion()==null||!Set.of("BUENO","DANADO").contains(f.estadoDevolucion()))
                throw NegocioException.invalido("Especifica el estado BUENO o DANADO de la herramienta devuelta");
            if("BUENO".equals(f.estadoDevolucion()))h.setCantidadDisponible(h.getCantidadDisponible().add(cantidad));

        } else if("INGRESO".equals(t)){
            h.setCantidadTotal(h.getCantidadTotal().add(cantidad));
            h.setCantidadDisponible(h.getCantidadDisponible().add(cantidad));
        } else throw NegocioException.invalido("Movimiento de herramienta no reconocido");
        MovimientoHerramientaEquipo mov=new MovimientoHerramientaEquipo();
        mov.setHerramientaEquipo(h);mov.setOrdenTrabajo(ot);mov.setEmpleadoResponsable(responsable);
        mov.setEmpleadoRegistra(actual.empleado());mov.setTipoMovimiento(t);mov.setCantidad(cantidad);
        mov.setEstadoDevolucion(f.estadoDevolucion());mov.setObservacion(f.observacion());
        herramientas.save(h);
        MovimientoHerramientaEquipo resultado = movimientos.saveAndFlush(mov);
        auditoriaEventos.publishEvent(EventoAuditoria.de("MOVIMIENTO_HERRAMIENTA", "MovimientoHerramientaEquipo", resultado.getId(), "Tipo: " + t));
        return resultado;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.almacen.HerramientaEquipo> listarCatalogo() {
        return herramientas.findAllByOrderByIdAsc();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo> listarMovimientos() {
        return movimientos.findAll();
    }

    @Override
    public MovimientoHerramientaEquipo registrarPrestamo(pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO datos) {
        if (!pe.edu.sigestec.enums.TipoMovimientoHerramienta.PRESTAMO.name().equals(datos.tipoMovimiento())) {
            throw NegocioException.invalido("Se requiere tipo PRESTAMO");
        }
        return registrar(datos);
    }

    @Override
    public MovimientoHerramientaEquipo registrarDevolucion(pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO datos) {
        if (!pe.edu.sigestec.enums.TipoMovimientoHerramienta.DEVOLUCION.name().equals(datos.tipoMovimiento())) {
            throw NegocioException.invalido("Se requiere tipo DEVOLUCION");
        }
        return registrar(datos);
    }

}
