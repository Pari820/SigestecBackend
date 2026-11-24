package pe.edu.sigestec.service.impl.solicitudes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.solicitudes.SolicitudServicioRequestDTO;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.catalogos.PersonaJuridicaRepository;
import pe.edu.sigestec.repository.catalogos.RepresentanteEmpresaRepository;
import pe.edu.sigestec.repository.catalogos.TipoServicioRepository;
import pe.edu.sigestec.repository.solicitudes.SolicitudServicioRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.solicitudes.SolicitudServicioService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class SolicitudServicioServiceImpl implements SolicitudServicioService {
    private final SolicitudServicioRepository solicitudes;
    private final PersonaJuridicaRepository empresas;
    private final TipoServicioRepository tipos;
    private final RepresentanteEmpresaRepository representantes;
    private final UsuarioActualService actual;
    private final ArchivoStorageService storage;
    private final CorrelativoDocumentoService correlativos;

    public SolicitudServicioServiceImpl(SolicitudServicioRepository s, PersonaJuridicaRepository e,
                                        TipoServicioRepository t, RepresentanteEmpresaRepository r, UsuarioActualService a, ArchivoStorageService storage,
                                        CorrelativoDocumentoService correlativos) {
        solicitudes = s;
        empresas = e;
        tipos = t;
        representantes = r;
        actual = a;
        this.storage = storage;
        this.correlativos = correlativos;
    }

    @Override
    public SolicitudServicio registrar(SolicitudServicioRequestDTO f, MultipartFile pdf) {
        PersonaJuridica pj = empresas.findById(f.clienteId()).orElseThrow(() -> NegocioException.noExiste("Empresa no encontrada"));
        TipoServicio ts = tipos.findById(f.tipoServicioId()).orElseThrow(() -> NegocioException.noExiste("Tipo de servicio no encontrado"));
        if (!"A".equals(pj.getEstado()) || !"A".equals(ts.getEstado()))
            throw NegocioException.conflicto("No se puede registrar: el cliente o el tipo de servicio está inactivo");
        SolicitudServicio s = new SolicitudServicio();
        s.setCodigoSolicitud(correlativos.siguiente("SOLICITUD", "SOL", s.getFechaSolicitud().getYear(), 3));
        s.setEmpresaCliente(pj);
        s.setTipoServicio(ts);
        if (f.representanteId() != null) {
            RepresentanteEmpresa r = representantes.findById(f.representanteId()).orElseThrow(() -> NegocioException.noExiste("Representante no encontrado"));
            if (!pj.getId().equals(r.getPersonaJuridica().getId()))
                throw NegocioException.invalido("El representante no pertenece a la empresa cliente");
            s.setRepresentanteSolicitante(r);
        }
        s.setDescripcion(f.descripcion().trim());
        s.setObservaciones(f.observaciones());
        s.setLugarServicio(f.lugarServicio());
        s.setEmpleadoRegistra(actual.empleado());
        s.setEstado("PENDIENTE");
        s.setUrlArchivoAdjunto(storage.guardar(pdf, false, true));
        return solicitudes.save(s);
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.solicitudes.SolicitudServicio> listar() {
        return solicitudes.findAllByOrderByFechaSolicitudAscIdAsc();
    }

}
