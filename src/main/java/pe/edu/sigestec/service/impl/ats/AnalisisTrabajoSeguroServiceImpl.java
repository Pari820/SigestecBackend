package pe.edu.sigestec.service.impl.ats;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AnalisisTrabajoSeguroRequestDTO;
import pe.edu.sigestec.dto.ats.AtsEtapaDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.ats.AnalisisEquipoProteccionPersonal;
import pe.edu.sigestec.entity.ats.AnalisisHerramientaEquipo;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.DetalleAnalisisRiesgo;
import pe.edu.sigestec.entity.ats.EquipoProteccionPersonal;
import pe.edu.sigestec.entity.ats.Riesgo;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.almacen.HerramientaEquipoRepository;
import pe.edu.sigestec.repository.ats.AnalisisEquipoProteccionPersonalRepository;
import pe.edu.sigestec.repository.ats.AnalisisHerramientaEquipoRepository;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.ats.DetalleAnalisisRiesgoRepository;
import pe.edu.sigestec.repository.ats.EquipoProteccionPersonalRepository;
import pe.edu.sigestec.repository.ats.RiesgoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.ats.AnalisisTrabajoSeguroService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class AnalisisTrabajoSeguroServiceImpl implements AnalisisTrabajoSeguroService {
    private final AnalisisTrabajoSeguroRepository analisis;
    private final DetalleAnalisisRiesgoRepository detalles;
    private final RiesgoRepository riesgos;
    private final EquipoProteccionPersonalRepository epps;
    private final HerramientaEquipoRepository herramientas;
    private final AnalisisEquipoProteccionPersonalRepository aEpp;
    private final AnalisisHerramientaEquipoRepository aHerr;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final ArchivoStorageService storage;
    private final CorrelativoDocumentoService correlativos;

    public AnalisisTrabajoSeguroServiceImpl(AnalisisTrabajoSeguroRepository a, DetalleAnalisisRiesgoRepository d,
                                            RiesgoRepository r, EquipoProteccionPersonalRepository ep, HerramientaEquipoRepository h,
                                            AnalisisEquipoProteccionPersonalRepository ap, AnalisisHerramientaEquipoRepository ah,
                                            ReferenciasService re, UsuarioActualService u, ArchivoStorageService storage,
                                            CorrelativoDocumentoService correlativos) {
        analisis = a;
        detalles = d;
        riesgos = r;
        epps = ep;
        herramientas = h;
        aEpp = ap;
        aHerr = ah;
        ref = re;
        actual = u;
        this.storage = storage;
        this.correlativos = correlativos;
    }

    private Riesgo riesgo(String texto) {
        return riesgos.findByNombreIgnoreCase(texto.trim()).orElseGet(() -> {
            Riesgo r = new Riesgo();
            r.setNombre(texto.trim());
            return riesgos.saveAndFlush(r);
        });
    }

    private EquipoProteccionPersonal equipo(String nombre) {
        return epps.findByNombreIgnoreCase(nombre.trim()).orElseGet(() -> {
            EquipoProteccionPersonal x = new EquipoProteccionPersonal();
            x.setNombre(nombre.trim());
            return epps.saveAndFlush(x);
        });
    }

    private HerramientaEquipo herramienta(String nombre) {
        return herramientas.findByNombreIgnoreCase(nombre.trim()).orElseGet(() -> {
            HerramientaEquipo h = new HerramientaEquipo();
            h.setCodigoHerramienta(correlativos.siguienteSinAnio("HERRAMIENTA", "HER", 6));
            h.setNombre(nombre.trim());
            return herramientas.saveAndFlush(h);
        });
    }

    @Override
    public AnalisisTrabajoSeguro elaborar(AnalisisTrabajoSeguroRequestDTO f, MultipartFile pdf) {
        OrdenTrabajo ot = ref.orden(f.ordenTrabajoId());
        if (!"PROGRAMADA".equals(ot.getEstado())) throw NegocioException.conflicto("La OT debe estar PROGRAMADA");
        if (analisis.existsByOrdenTrabajoId(ot.getId()))
            throw NegocioException.conflicto("Esta OT ya tiene un análisis de trabajo seguro");
        if (f.peligros() == null || f.peligros().isEmpty() || f.epp() == null || f.epp().isEmpty() ||
                f.etapas() == null || f.etapas().isEmpty())
            throw NegocioException.invalido("Debe registrar riesgos, EPP y etapas");
        AnalisisTrabajoSeguro a = new AnalisisTrabajoSeguro();
        a.setNumeroAts(correlativos.siguiente("ATS", "ATS", a.getFechaRegistro().getYear(), 3));
        a.setOrdenTrabajo(ot);
        a.setEmpresaCliente(ot.getEmpresaCliente());
        a.setEmpleadoRegistra(actual.empleado());
        a.setEmpleadoElabora(actual.empleado());
        a.setCondicionesArea(f.condicionesArea().trim());


        a.setObservaciones("Peligros identificados: " + String.join(" | ", f.peligros()));
        a.setUrlArchivo(storage.guardar(pdf, false, true));
        a.setEstado("PENDIENTE");
        analisis.saveAndFlush(a);
        for (AtsEtapaDTO e : f.etapas()) {
            if (e.riesgo() == null || e.riesgo().isBlank() || e.etapa() == null || e.etapa().isBlank() ||
                    e.peligro() == null || e.peligro().isBlank() || e.medidaControl() == null || e.medidaControl().isBlank())
                throw NegocioException.invalido("Etapa incompleta");
            DetalleAnalisisRiesgo d = new DetalleAnalisisRiesgo();
            d.setAnalisisTrabajoSeguro(a);
            d.setRiesgo(riesgo(e.riesgo()));
            d.setEtapaTrabajo(e.etapa().trim());
            d.setActividad(e.etapa().trim());
            d.setPeligro(e.peligro().trim());
            d.setMedidaControl(e.medidaControl().trim());
            d.setNivelRiesgo(e.nivel());
            detalles.save(d);
        }
        for (String nombre : new LinkedHashSet<>(f.epp())) {
            AnalisisEquipoProteccionPersonal x = new AnalisisEquipoProteccionPersonal();
            x.setAnalisisTrabajoSeguro(a);
            x.setEquipoProteccionPersonal(equipo(nombre));
            x.setCantidad(BigDecimal.ONE);
            aEpp.save(x);
        }
        if (f.herramientas() != null && !f.herramientas().isBlank()) {
            for (String nombre : new LinkedHashSet<>(Arrays.asList(f.herramientas().split("[,;\\n]+")))) {
                if (nombre.isBlank()) continue;
                AnalisisHerramientaEquipo x = new AnalisisHerramientaEquipo();
                x.setAnalisisTrabajoSeguro(a);
                x.setHerramientaEquipo(herramienta(nombre));
                x.setCantidadRequerida(BigDecimal.ONE);
                aHerr.save(x);
            }
        }
        return a;
    }
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro> listar() {
        return analisis.findAllByOrderByFechaRegistroAscIdAsc();
    }

}
