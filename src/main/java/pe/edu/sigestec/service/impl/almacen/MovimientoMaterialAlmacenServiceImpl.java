package pe.edu.sigestec.service.impl.almacen;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.almacen.MaterialRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.almacen.MaterialRepository;
import pe.edu.sigestec.repository.almacen.MovimientoMaterialAlmacenRepository;
import pe.edu.sigestec.repository.ejecuciones.MaterialUtilizadoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.almacen.MovimientoMaterialAlmacenService;

@Service
@Transactional
public class MovimientoMaterialAlmacenServiceImpl implements MovimientoMaterialAlmacenService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final CorrelativoDocumentoService correlativos;
    private final MaterialRepository materiales;
    private final MovimientoMaterialAlmacenRepository movimientos;
    private final MaterialUtilizadoRepository materialesUsados;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    public MovimientoMaterialAlmacenServiceImpl(MaterialRepository m, MovimientoMaterialAlmacenRepository mo, MaterialUtilizadoRepository mu, ReferenciasService r, UsuarioActualService u, CorrelativoDocumentoService correlativos) {
        materiales=m;movimientos=mo;materialesUsados=mu;ref=r;actual=u;this.correlativos=correlativos;
    }
    @Override
    public Material crear(MaterialRequestDTO f){
        Material m=new Material();m.setCodigo(correlativos.siguienteSinAnio("MATERIAL", "MAT", 6));
        m.setNombre(f.nombre().trim());
        m.setUnidadMedida(f.unidadMedida().trim());m.setStockActual(f.stockInicial());m.setStockMinimo(f.stockMinimo());
        m.setEstado("A");Material resultado = materiales.saveAndFlush(m);
        auditoriaEventos.publishEvent(EventoAuditoria.de("MATERIAL_REGISTRADO", "Material", resultado.getId(), "Material agregado al catálogo"));
        return resultado;
    }
    @Override
    public MovimientoMaterialAlmacen registrar(MovimientoMaterialRequestDTO f){
        Material m=materiales.buscarParaActualizar(f.materialId()).orElseThrow(()->NegocioException.noExiste("Material no encontrado"));
        if(!"A".equals(m.getEstado()))throw NegocioException.conflicto("Material inactivo");
        String t=f.tipoMovimiento();
        OrdenTrabajo ot=null;
        if(!"ENTRADA".equals(t)){
            if(f.ordenTrabajoId()==null)throw NegocioException.invalido("Indica la OT para salidas y devoluciones");
            ot=ref.orden(f.ordenTrabajoId());
            if(!java.util.Set.of("PROGRAMADA","EJECUTADA","ATENDIDA").contains(ot.getEstado()))
                throw NegocioException.conflicto("La OT no se encuentra habilitada para movimientos");
        }
        BigDecimal cantidad=f.cantidad();
        if("SALIDA".equals(t)){
            if(m.getStockActual().compareTo(cantidad)<0)throw NegocioException.conflicto("Stock insuficiente para la salida");
            m.setStockActual(m.getStockActual().subtract(cantidad));
        } else if("DEVOLUCION".equals(t)) {
            BigDecimal saldo=BigDecimal.ZERO;
            for(MovimientoMaterialAlmacen mov:movimientos.findByOrdenTrabajoIdAndMaterialId(ot.getId(),m.getId())){
                if("SALIDA".equals(mov.getTipoMovimiento()))saldo=saldo.add(mov.getCantidad());
                if("DEVOLUCION".equals(mov.getTipoMovimiento()))saldo=saldo.subtract(mov.getCantidad());
            }
            if(saldo.compareTo(cantidad)<0)throw NegocioException.conflicto("La devolución supera la cantidad entregada a esta OT");
            m.setStockActual(m.getStockActual().add(cantidad));
        } else if("ENTRADA".equals(t)) {
            m.setStockActual(m.getStockActual().add(cantidad));
        } else throw NegocioException.invalido("Tipo de movimiento no soportado");
        MovimientoMaterialAlmacen mov=new MovimientoMaterialAlmacen();
        mov.setMaterial(m);mov.setOrdenTrabajo(ot);mov.setEmpleadoRegistra(actual.empleado());
        mov.setTipoMovimiento(t);mov.setCantidad(cantidad);mov.setMotivo(f.motivo());mov.setObservacion(f.observacion());
        materiales.save(m);
        movimientos.saveAndFlush(mov);
        if(ot!=null){
            var declarados=materialesUsados.findByOtYMaterial(ot.getId(),m.getId());
            if(!declarados.isEmpty()){
                BigDecimal declarado=declarados.stream().map(MaterialUtilizado::getCantidadReal).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal salidaNeta=BigDecimal.ZERO;
                for(MovimientoMaterialAlmacen movimiento:movimientos.findByOrdenTrabajoIdAndMaterialId(ot.getId(),m.getId())){
                    if("SALIDA".equals(movimiento.getTipoMovimiento()))salidaNeta=salidaNeta.add(movimiento.getCantidad());
                    if("DEVOLUCION".equals(movimiento.getTipoMovimiento()))salidaNeta=salidaNeta.subtract(movimiento.getCantidad());
                }
                String estado=salidaNeta.compareTo(declarado)>=0?"VALIDADO":"PENDIENTE";
                declarados.forEach(d->{d.setEstadoValidacion(estado);materialesUsados.save(d);});
            }
        }
        auditoriaEventos.publishEvent(EventoAuditoria.de("MOVIMIENTO_MATERIAL", "MovimientoMaterialAlmacen", mov.getId(), "Tipo: " + t));
        return mov;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.almacen.Material> listarCatalogo() {
        return materiales.findAllByOrderByIdAsc();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen> listarMovimientos() {
        return movimientos.findAll();
    }

    @Override
    public MovimientoMaterialAlmacen registrarDevolucion(pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO datos) {
        if (!pe.edu.sigestec.enums.TipoMovimientoMaterial.DEVOLUCION.name().equals(datos.tipoMovimiento())) {
            throw pe.edu.sigestec.exception.NegocioException.invalido("Se requiere tipo DEVOLUCION");
        }
        return registrar(datos);
    }

}
