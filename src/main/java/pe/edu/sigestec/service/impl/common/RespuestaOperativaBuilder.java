package pe.edu.sigestec.service.impl.common;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioResponseDTO;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioResponseDTO;
import pe.edu.sigestec.dto.informes.InformeTecnicoResponseDTO;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.entity.ats.AprobacionAts;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.mapper.common.VistaCamposMapper;
import pe.edu.sigestec.repository.ejecuciones.EvidenciaEjecucionRepository;
import pe.edu.sigestec.repository.ejecuciones.MaterialUtilizadoRepository;
import pe.edu.sigestec.repository.informes.EvidenciaInformeTecnicoRepository;

@Service
@Transactional(readOnly=true)
public class RespuestaOperativaBuilder {
    private final RespuestaComercialBuilder vistas;
    private final VistaCamposMapper campos;
    private final EvidenciaEjecucionRepository evidenciasEjecucion;
    private final MaterialUtilizadoRepository materialUtilizado;
    private final EvidenciaInformeTecnicoRepository evidenciasInforme;
    public RespuestaOperativaBuilder(RespuestaComercialBuilder v,EvidenciaEjecucionRepository ee,MaterialUtilizadoRepository mu,EvidenciaInformeTecnicoRepository ei, VistaCamposMapper campos){
        this.campos = campos;
        vistas=v;evidenciasEjecucion=ee;materialUtilizado=mu;evidenciasInforme=ei;
    }
    private static String txt(String x){return x==null?"":x;}
    public Map<String,Object> aprobacion(AprobacionAts x){
        Map<String,Object> m=new LinkedHashMap<>();m.put("id",x.getId());
        m.put("atsId",x.getAnalisisTrabajoSeguro().getId());m.put("atsNumero",x.getAnalisisTrabajoSeguro().getNumeroAts());
        m.put("ordenTrabajoId",x.getAnalisisTrabajoSeguro().getOrdenTrabajo().getId());
        m.put("ordenTrabajoNumero",x.getAnalisisTrabajoSeguro().getOrdenTrabajo().getNumeroOrdenTrabajo());
        m.put("supervisorCliente",x.getSupervisorCliente());m.put("ssomaCliente",x.getSsomaCliente());
        m.put("fechaRevision",x.getFechaRevision());m.put("resultado",x.getResultado());
        m.put("observaciones",txt(x.getObservaciones()));m.put("urlActa",x.getUrlActa());
        m.put("empleadoRegistra",vistas.empleado(x.getEmpleadoRegistra()));return m;
    }
    public EjecucionServicioResponseDTO ejecucion(EjecucionServicio x) {
        List<?> evidencias = evidenciasEjecucion.findByEjecucionServicioId(x.getId()).stream()
            .map(v -> Map.of("id", v.getId(), "etapa", v.getEtapa(),
                             "urlArchivo", v.getUrlArchivo(), "nombreArchivo", txt(v.getNombreArchivo())))
            .toList();
        List<?> materiales = materialUtilizado.findByEjecucionServicioId(x.getId()).stream()
            .map(d -> Map.of("id", d.getId(), "material", material(d.getMaterial()),
                             "cantidadReal", d.getCantidadReal(), "estadoValidacion", d.getEstadoValidacion()))
            .toList();
        return new EjecucionServicioResponseDTO(
            x.getId(), x.getNumeroEjecucion(), vistas.orden(x.getOrdenTrabajo()), x.getFechaEjecucion(),
            x.getDescripcion(), txt(x.getMediciones()), txt(x.getObservaciones()),
            x.getEstado(), vistas.empleado(x.getEmpleadoRegistra()), evidencias, materiales
        );
    }
    public InformeTecnicoResponseDTO informe(InformeTecnico x) {
        List<?> evidencias = evidenciasInforme.findByInformeTecnicoId(x.getId()).stream()
            .map(v -> Map.of("id", v.getId(), "urlArchivo", v.getUrlArchivo(),
                             "nombreArchivo", txt(v.getNombreArchivo())))
            .toList();
        return new InformeTecnicoResponseDTO(
            x.getId(), vistas.orden(x.getOrdenTrabajo()), x.getEjecucionServicio().getId(),
            x.getNumeroInforme(), x.getFechaInforme(), x.getFechaEnvio(),
            x.getDiagnostico(), x.getTrabajosRealizados(), x.getConclusiones(),
            txt(x.getRecomendaciones()), x.getEstado(), x.getUrlArchivo(),
            vistas.empleado(x.getEmpleadoRegistra()), evidencias
        );
    }
    public ConformidadServicioResponseDTO conformidad(ConformidadServicio x) {
        return new ConformidadServicioResponseDTO(
            x.getId(), vistas.orden(x.getOrdenTrabajo()), x.getInformeTecnico().getId(),
            x.getNumeroActa(), x.getFechaConformidad(), x.getNombreRepresentante(),
            x.getValoracion(), x.getEstadoConformidad(), txt(x.getObservaciones()),
            x.getUrlArchivo(), x.getRepresentanteEmpresa() == null ? null : x.getRepresentanteEmpresa().getId(),
            vistas.empleado(x.getEmpleadoRegistra())
        );
    }
    public Map<String, Object> material(Material x) {
        return campos.material(x);
    }

    public Map<String,Object> movimientoMaterial(MovimientoMaterialAlmacen x){
        Map<String,Object> m=new LinkedHashMap<>();m.put("id",x.getId());m.put("material",material(x.getMaterial()));
        m.put("ordenTrabajoId",x.getOrdenTrabajo()==null?null:x.getOrdenTrabajo().getId());
        m.put("ordenTrabajoNumero",x.getOrdenTrabajo()==null?null:x.getOrdenTrabajo().getNumeroOrdenTrabajo());
        m.put("tipoMovimiento",x.getTipoMovimiento());m.put("cantidad",x.getCantidad());
        m.put("fechaMovimiento",x.getFechaMovimiento());m.put("motivo",txt(x.getMotivo()));m.put("observacion",txt(x.getObservacion()));
        m.put("empleadoRegistra",vistas.empleado(x.getEmpleadoRegistra()));return m;
    }
    public Map<String, Object> herramienta(HerramientaEquipo x) {
        return campos.herramienta(x);
    }

    public Map<String,Object> movimientoHerramienta(MovimientoHerramientaEquipo x){
        Map<String,Object> m=new LinkedHashMap<>();m.put("id",x.getId());m.put("herramienta",herramienta(x.getHerramientaEquipo()));
        m.put("ordenTrabajoId",x.getOrdenTrabajo()==null?null:x.getOrdenTrabajo().getId());
        m.put("ordenTrabajoNumero",x.getOrdenTrabajo()==null?null:x.getOrdenTrabajo().getNumeroOrdenTrabajo());
        m.put("empleadoResponsable",x.getEmpleadoResponsable()==null?null:vistas.empleado(x.getEmpleadoResponsable()));
        m.put("tipoMovimiento",x.getTipoMovimiento());m.put("cantidad",x.getCantidad());
        m.put("fechaMovimiento",x.getFechaMovimiento());m.put("estadoDevolucion",txt(x.getEstadoDevolucion()));
        m.put("observacion",txt(x.getObservacion()));m.put("empleadoRegistra",vistas.empleado(x.getEmpleadoRegistra()));return m;
    }
}
