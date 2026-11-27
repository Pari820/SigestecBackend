package pe.edu.sigestec.service.impl.catalogos;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;

import org.springframework.data.domain.Sort;
import pe.edu.sigestec.service.CorrelativoDocumentoService;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.catalogos.ClienteRequestDTO;
import pe.edu.sigestec.dto.catalogos.TipoServicioRequestDTO;
import pe.edu.sigestec.dto.personal.PersonalRequestDTO;
import pe.edu.sigestec.entity.auth.Perfil;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.personal.Cargo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.personal.Tecnico;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.auth.PerfilRepository;
import pe.edu.sigestec.repository.catalogos.PersonaJuridicaRepository;
import pe.edu.sigestec.repository.catalogos.PersonaNaturalRepository;
import pe.edu.sigestec.repository.catalogos.RepresentanteEmpresaRepository;
import pe.edu.sigestec.repository.catalogos.TipoServicioRepository;
import pe.edu.sigestec.repository.personal.CargoRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.repository.personal.TecnicoRepository;
import pe.edu.sigestec.service.impl.common.RespuestaComercialBuilder;
import pe.edu.sigestec.service.service.catalogos.CatalogosService;

@Service
@Transactional
public class CatalogosServiceImpl implements CatalogosService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final PerfilRepository perfiles;
    private final PersonaNaturalRepository naturales;
    private final PersonaJuridicaRepository empresas;
    private final RepresentanteEmpresaRepository representantes;
    private final TipoServicioRepository tipos;
    private final EmpleadoRepository empleados;
    private final CargoRepository cargos;
    private final TecnicoRepository tecnicos;
    private final RespuestaComercialBuilder vistas;
    private final CorrelativoDocumentoService correlativos;


    public CatalogosServiceImpl(PerfilRepository p, PersonaNaturalRepository pn, PersonaJuridicaRepository pj,
                               RepresentanteEmpresaRepository r, TipoServicioRepository t, EmpleadoRepository e, CargoRepository c, TecnicoRepository te, RespuestaComercialBuilder v, CorrelativoDocumentoService correlativos) {
        perfiles = p;
        naturales = pn;
        empresas = pj;
        representantes = r;
        tipos = t;
        empleados = e;
        cargos = c;
        tecnicos = te;
        vistas = v;
        this.correlativos = correlativos;
    }

    public List<?> clientes() {
        return empresas.findAll(Sort.by(Sort.Direction.ASC, "numeroRegistro")).stream().map(vistas::cliente).toList();
    }

    public Object cliente(ClienteRequestDTO f) {
        if (empresas.findByRuc(f.ruc()).isPresent()) throw NegocioException.conflicto("RUC ya registrado");
        Perfil p = new Perfil();
        p.setTipoPerfil("JURIDICA");
        perfiles.save(p);
        PersonaJuridica pj = new PersonaJuridica();
        pj.setPerfil(p);
        pj.setNumeroRegistro(correlativos.siguienteNumeroSinAnio("CLIENTE"));
        pj.setRuc(f.ruc());
        pj.setRazonSocial(f.razonSocial().trim());
        pj.setTelefono(f.telefono());
        empresas.save(pj);
        if (f.contacto() != null && !f.contacto().isBlank()) {
            Perfil perfil = new Perfil();
            perfil.setTipoPerfil("NATURAL");
            perfiles.save(perfil);
            PersonaNatural pn = new PersonaNatural();
            pn.setPerfil(perfil);
            String nombre = f.contacto().trim();
            int i = nombre.lastIndexOf(' ');
            pn.setNombres(i < 0 ? nombre : nombre.substring(0, i));
            pn.setApellidos(i < 0 ? "No consignado" : nombre.substring(i + 1));
            naturales.save(pn);
            RepresentanteEmpresa rep = new RepresentanteEmpresa();
            rep.setPersonaNatural(pn);
            rep.setPersonaJuridica(pj);
            rep.setTipoRepresentante("CONTACTO");
            representantes.save(rep);
        }
        auditoriaEventos.publishEvent(EventoAuditoria.de("CLIENTE_REGISTRADO", "PersonaJuridica", pj.getId(), "Cliente registrado"));
        return vistas.cliente(pj);
    }

    public List<?> tipos() {
        return tipos.findAll(Sort.by(Sort.Direction.ASC, "numeroRegistro")).stream().map(vistas::tipo).toList();
    }

    public Object tipo(TipoServicioRequestDTO f) {
        if (tipos.findByNombreIgnoreCase(f.nombre()).isPresent())
            throw NegocioException.conflicto("Servicio duplicado");
        TipoServicio t = new TipoServicio();
        t.setNumeroRegistro(correlativos.siguienteNumeroSinAnio("TIPO_SERVICIO"));
        t.setNombre(f.nombre().trim());
        t.setDescripcion(f.descripcion());
        TipoServicio guardado = tipos.save(t);
        auditoriaEventos.publishEvent(EventoAuditoria.de("TIPO_SERVICIO_REGISTRADO", "TipoServicio", guardado.getId(), "Tipo de servicio registrado"));
        return vistas.tipo(guardado);
    }

    public List<?> personal() {
        return empleados.findAll(Sort.by(Sort.Direction.ASC, "numeroRegistro")).stream().map(vistas::empleado).toList();
    }

    public Object personal(PersonalRequestDTO f) {
        if (!Set.of("TECNICO", "SSOMA", "APOYO").contains(f.perfil()))
            throw NegocioException.invalido("Perfil no válido");
        Cargo cargo = cargos.findByNombreIgnoreCase(f.perfil()).orElseGet(() -> {
            Cargo c = new Cargo();
            c.setNombre(f.perfil());
            return cargos.save(c);
        });
        String dni = f.dni() == null ? "" : f.dni().trim();
        if (!dni.isEmpty() && naturales.findByDni(dni).isPresent())
            throw NegocioException.conflicto("Este DNI ya está registrado en el sistema.");
        String nombres = f.nombres() == null ? "" : f.nombres().trim();
        String apellidos = f.apellidos() == null ? "" : f.apellidos().trim();
        if (nombres.isEmpty() != apellidos.isEmpty())
            throw NegocioException.invalido("Completa nombres y apellidos para registrar el personal.");
        if (nombres.isEmpty()) {
            String nombreCompleto = f.nombre().trim();
            int separacion = nombreCompleto.lastIndexOf(' ');
            nombres = separacion < 0 ? nombreCompleto : nombreCompleto.substring(0, separacion);
            apellidos = separacion < 0 ? "No consignado" : nombreCompleto.substring(separacion + 1);
        }
        Perfil p = new Perfil();
        p.setTipoPerfil("NATURAL");
        perfiles.save(p);
        PersonaNatural pn = new PersonaNatural();
        pn.setPerfil(p);
        pn.setDni(dni.isEmpty() ? null : dni);
        pn.setNombres(nombres);
        pn.setApellidos(apellidos);
        naturales.save(pn);
        Empleado e = new Empleado();
        e.setNumeroRegistro(correlativos.siguienteNumeroSinAnio("PERSONAL"));
        e.setPersonaNatural(pn);
        e.setCargo(cargo);
        e.setCodigoEmpleado("EMP-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        empleados.saveAndFlush(e);
        if ("TECNICO".equals(f.perfil())) {
            Tecnico te = new Tecnico();
            te.setEmpleado(e);
            tecnicos.save(te);
        }
        auditoriaEventos.publishEvent(EventoAuditoria.de("PERSONAL_REGISTRADO", "Empleado", e.getId(), "Personal registrado"));
        return vistas.empleado(e);
    }
}
