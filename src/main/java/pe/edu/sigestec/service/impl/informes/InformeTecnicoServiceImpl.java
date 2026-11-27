package pe.edu.sigestec.service.impl.informes;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.informes.InformeTecnicoRequestDTO;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.informes.EvidenciaInformeTecnico;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.ejecuciones.EjecucionServicioRepository;
import pe.edu.sigestec.repository.informes.EvidenciaInformeTecnicoRepository;
import pe.edu.sigestec.repository.informes.InformeTecnicoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.informes.InformeTecnicoService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class InformeTecnicoServiceImpl implements InformeTecnicoService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final InformeTecnicoRepository informes;
    private final EjecucionServicioRepository ejecuciones;
    private final EvidenciaInformeTecnicoRepository evidencias;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService archivos;
    private final CorrelativoDocumentoService correlativos;
    public InformeTecnicoServiceImpl(InformeTecnicoRepository i, EjecucionServicioRepository e,
              EvidenciaInformeTecnicoRepository v, ReferenciasService r, UsuarioActualService u, ArchivoStorageService f,
              CorrelativoDocumentoService correlativos){
        informes=i;ejecuciones=e;evidencias=v;ref=r;actual=u;archivos=f;this.correlativos=correlativos;
    }
    @Override
    public InformeTecnico registrar(InformeTecnicoRequestDTO f, MultipartFile pdf, List<MultipartFile> fotos){
        OrdenTrabajo ot=ref.orden(f.ordenTrabajoId());
        if(!"EJECUTADA".equals(ot.getEstado()))throw NegocioException.conflicto("La OT debe tener una ejecución registrada");
        EjecucionServicio e=ejecuciones.findByOrdenTrabajoId(ot.getId()).orElseThrow(()->NegocioException.conflicto("No existe ejecución"));
        if(informes.existsByOrdenTrabajoId(ot.getId()))throw NegocioException.conflicto("La OT ya tiene un informe técnico");
        if (ot.getFechaProgramadaFin() == null)
            throw NegocioException.conflicto("La OT no tiene fecha de fin programada");
        // Se permiten fechas simuladas futuras; solo se valida
        // que el envío esté fechado después del fin programado de la OT.
        LocalDate fechaMinima = ReglasFechaServicio.primeraFechaInforme(ot.getFechaProgramadaFin());
        if (!ReglasFechaServicio.fechaInformeValida(ot.getFechaProgramadaFin(), f.fechaEnvio()))
            throw NegocioException.invalido("La fecha de envío debe ser desde " + fechaMinima + " en adelante");
        InformeTecnico i=new InformeTecnico();
        i.setOrdenTrabajo(ot);i.setEjecucionServicio(e);i.setEmpleadoRegistra(actual.empleado());
        i.setDiagnostico(f.diagnostico().trim());i.setTrabajosRealizados(f.trabajosRealizados().trim());
        i.setConclusiones(f.conclusiones().trim());i.setRecomendaciones(f.recomendaciones());
        i.setFechaEnvio(f.fechaEnvio());i.setEstado("ENVIADO");
        i.setNumeroInforme(correlativos.siguiente("INFORME_TECNICO", "INF", i.getFechaInforme().getYear(), 3));
        i.setUrlArchivo(archivos.guardar(pdf,false,true));
        informes.saveAndFlush(i);
        if(fotos!=null)for(MultipartFile foto:fotos){
            if(foto==null||foto.isEmpty())continue;
            String tipo=Objects.toString(foto.getContentType(),"");
            if(!tipo.equals("image/png") && !tipo.equals("image/jpeg"))throw NegocioException.invalido("La evidencia debe ser PNG o JPG");
            EvidenciaInformeTecnico v=new EvidenciaInformeTecnico();v.setInformeTecnico(i);
            v.setUrlArchivo(archivos.guardar(foto,true,false));v.setNombreArchivo(foto.getOriginalFilename());evidencias.save(v);
        }
        auditoriaEventos.publishEvent(EventoAuditoria.de("INFORME_TECNICO_REGISTRADO", "InformeTecnico", i.getId(), "Informe técnico registrado"));
        return i;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.informes.InformeTecnico> listar() {
        return informes.findAllByOrderByFechaInformeAscIdAsc();
    }

}
