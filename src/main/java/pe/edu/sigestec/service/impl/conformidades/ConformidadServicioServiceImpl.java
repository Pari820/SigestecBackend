package pe.edu.sigestec.service.impl.conformidades;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioRequestDTO;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.catalogos.RepresentanteEmpresaRepository;
import pe.edu.sigestec.repository.conformidades.ConformidadServicioRepository;
import pe.edu.sigestec.repository.informes.InformeTecnicoRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.conformidades.ConformidadServicioService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class ConformidadServicioServiceImpl implements ConformidadServicioService {
    private final ConformidadServicioRepository conformidades;
    private final InformeTecnicoRepository informes;
    private final RepresentanteEmpresaRepository representantes;
    private final OrdenTrabajoRepository ordenes;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService archivos;
    private final CorrelativoDocumentoService correlativos;
    public ConformidadServicioServiceImpl(ConformidadServicioRepository c,InformeTecnicoRepository i,
             RepresentanteEmpresaRepository r, OrdenTrabajoRepository o, ReferenciasService re,
             UsuarioActualService u, ArchivoStorageService f, CorrelativoDocumentoService correlativos){
        conformidades=c;informes=i;representantes=r;ordenes=o;ref=re;actual=u;archivos=f;
        this.correlativos=correlativos;
    }
    @Override
    public ConformidadServicio registrar(ConformidadServicioRequestDTO f,MultipartFile acta){
        OrdenTrabajo ot=ref.orden(f.ordenTrabajoId());
        InformeTecnico i=informes.findByOrdenTrabajoId(ot.getId()).orElseThrow(()->NegocioException.conflicto("Primero se debe registrar el informe técnico"));
        if(!"ENVIADO".equals(i.getEstado()))throw NegocioException.conflicto("El informe debe estar ENVIADO al cliente");
        if(conformidades.existsByOrdenTrabajoId(ot.getId()))throw NegocioException.conflicto("Ya existe conformidad para esta OT");
        if (i.getFechaEnvio() == null)
            throw NegocioException.conflicto("El informe técnico no tiene fecha de envío");
        if (!ReglasFechaServicio.fechaConformidadValida(i.getFechaEnvio(), f.fechaConformidad()))
            throw NegocioException.invalido("La fecha de conformidad debe ser igual o posterior al envío del informe (" + i.getFechaEnvio() + ")");
        if(acta==null||acta.isEmpty())throw NegocioException.invalido("Adjunta el acta firmada (PDF, PNG o JPG)");
        ConformidadServicio c=new ConformidadServicio();c.setOrdenTrabajo(ot);c.setInformeTecnico(i);
        c.setEmpleadoRegistra(actual.empleado());
        c.setNumeroActa(correlativos.siguienteConAnioDinamico(
                "ACTA_CONFORMIDAD", "ACTA", f.fechaConformidad().getYear(), 3));
        c.setNombreRepresentante(f.nombreRepresentante().trim());c.setFechaConformidad(f.fechaConformidad());
        c.setValoracion(f.valoracion());c.setEstadoConformidad(f.estadoConformidad());c.setObservaciones(f.observaciones());
        if(f.representanteEmpresaId()!=null){
            RepresentanteEmpresa rep=representantes.findById(f.representanteEmpresaId()).orElseThrow(()->NegocioException.noExiste("Representante no encontrado"));
            if(!rep.getPersonaJuridica().getId().equals(ot.getEmpresaCliente().getId()))throw NegocioException.invalido("El representante no pertenece a la empresa cliente");
            c.setRepresentanteEmpresa(rep);
        }
        c.setUrlArchivo(archivos.guardar(acta,true,false));conformidades.saveAndFlush(c);
        if("CONFORME".equals(c.getEstadoConformidad())) {ot.setEstado("ATENDIDA");ordenes.save(ot);}
        return c;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.conformidades.ConformidadServicio> listar() {
        return conformidades.findAll();
    }

}
