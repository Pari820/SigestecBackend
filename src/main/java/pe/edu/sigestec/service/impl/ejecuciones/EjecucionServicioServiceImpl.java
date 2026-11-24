package pe.edu.sigestec.service.impl.ejecuciones;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioRequestDTO;
import pe.edu.sigestec.dto.ejecuciones.MaterialRealDTO;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.ejecuciones.EvidenciaEjecucion;
import pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.almacen.MaterialRepository;
import pe.edu.sigestec.repository.almacen.MovimientoMaterialAlmacenRepository;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.ejecuciones.EjecucionServicioRepository;
import pe.edu.sigestec.repository.ejecuciones.EvidenciaEjecucionRepository;
import pe.edu.sigestec.repository.ejecuciones.MaterialUtilizadoRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.ejecuciones.EjecucionServicioService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class EjecucionServicioServiceImpl implements EjecucionServicioService {
    private final EjecucionServicioRepository ejecuciones;
    private final EvidenciaEjecucionRepository evidencias;
    private final MaterialUtilizadoRepository materialesUsados;
    private final MaterialRepository materiales;
    private final MovimientoMaterialAlmacenRepository movimientosMaterial;
    private final AnalisisTrabajoSeguroRepository ats;
    private final OrdenTrabajoRepository ordenes;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService archivos;
    private final CorrelativoDocumentoService correlativos;
    public EjecucionServicioServiceImpl(EjecucionServicioRepository e, EvidenciaEjecucionRepository ev,
          MaterialUtilizadoRepository mu, MaterialRepository m, MovimientoMaterialAlmacenRepository mm, AnalisisTrabajoSeguroRepository a,
          OrdenTrabajoRepository o, ReferenciasService r, UsuarioActualService u, ArchivoStorageService f,
          CorrelativoDocumentoService correlativos) {
        ejecuciones=e; evidencias=ev; materialesUsados=mu; materiales=m; movimientosMaterial=mm; ats=a; ordenes=o; ref=r; actual=u; archivos=f; this.correlativos=correlativos;
    }
    @Override
    public EjecucionServicio registrar(EjecucionServicioRequestDTO f, MultipartFile antes, MultipartFile durante, MultipartFile despues) {
        OrdenTrabajo ot=ref.orden(f.ordenTrabajoId());
        if (!"PROGRAMADA".equals(ot.getEstado())) throw NegocioException.conflicto("La OT debe estar PROGRAMADA");
        if (ejecuciones.existsByOrdenTrabajoId(ot.getId())) throw NegocioException.conflicto("La OT ya registra una ejecución");
        boolean autorizado=ats.findAll().stream().anyMatch(a->a.getOrdenTrabajo().getId().equals(ot.getId()) && "APROBADO".equals(a.getEstado()));
        if (!autorizado) throw NegocioException.conflicto("Se requiere un ATS APROBADO para ejecutar la OT");
        if ((antes==null||antes.isEmpty()) && (durante==null||durante.isEmpty()) && (despues==null||despues.isEmpty()))
            throw NegocioException.invalido("Adjunta al menos una fotografía de evidencia");
        EjecucionServicio e=new EjecucionServicio();
        e.setNumeroEjecucion(correlativos.siguiente("EJECUCION", "EJE", e.getFechaEjecucion().getYear(), 3));
        e.setOrdenTrabajo(ot); e.setEmpleadoRegistra(actual.empleado());
        e.setDescripcion(f.descripcion().trim());e.setMediciones(f.mediciones()); e.setObservaciones(f.observaciones());
        e.setEstado("REGISTRADA"); ejecuciones.saveAndFlush(e);
        guardarFoto(e,"ANTES",antes);guardarFoto(e,"DURANTE",durante);guardarFoto(e,"DESPUES",despues);
        Set<Long> materialIds=new HashSet<>();
        if (f.materiales()!=null) for (MaterialRealDTO item:f.materiales()) {
            if(!materialIds.add(item.materialId()))throw NegocioException.invalido("El material está repetido en la ejecución");
            Material m=materiales.findById(item.materialId()).orElseThrow(()->NegocioException.noExiste("Material no encontrado"));
            MaterialUtilizado mu=new MaterialUtilizado();mu.setEjecucionServicio(e);mu.setMaterial(m);
            mu.setCantidadReal(item.cantidadReal());
            java.math.BigDecimal salidaNeta=java.math.BigDecimal.ZERO;
            for(MovimientoMaterialAlmacen mov:movimientosMaterial.findByOrdenTrabajoIdAndMaterialId(ot.getId(),m.getId())){
                if("SALIDA".equals(mov.getTipoMovimiento()))salidaNeta=salidaNeta.add(mov.getCantidad());
                if("DEVOLUCION".equals(mov.getTipoMovimiento()))salidaNeta=salidaNeta.subtract(mov.getCantidad());
            }
            mu.setEstadoValidacion(salidaNeta.compareTo(item.cantidadReal())>=0?"VALIDADO":"PENDIENTE");
            materialesUsados.save(mu);
        }
        ot.setEstado("EJECUTADA");ordenes.save(ot);
        return e;
    }
    private void guardarFoto(EjecucionServicio e,String etapa,MultipartFile foto) {
        if(foto==null || foto.isEmpty())return;
        String tipo=Objects.toString(foto.getContentType(),"");
        if(!tipo.equals("image/png")&&!tipo.equals("image/jpeg")) throw NegocioException.invalido("La evidencia debe ser PNG o JPG");
        EvidenciaEjecucion v=new EvidenciaEjecucion();v.setEjecucionServicio(e);v.setEtapa(etapa);
        v.setUrlArchivo(archivos.guardar(foto,true,false));v.setNombreArchivo(foto.getOriginalFilename());evidencias.save(v);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.ejecuciones.EjecucionServicio> listar() {
        return ejecuciones.findAllByOrderByFechaEjecucionAscIdAsc();
    }

}
