package pe.edu.sigestec.service.impl.asignaciones;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.asignaciones.AsignacionPersonalRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.orden_trabajo.CambioEquipoRequestDTO;
import pe.edu.sigestec.entity.asignaciones.AsignacionPersonal;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.asignaciones.AsignacionPersonalRepository;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.compras.OrdenCompraRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.asignaciones.AsignacionPersonalService;

@Service
@Transactional
public class AsignacionPersonalServiceImpl implements AsignacionPersonalService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    private final OrdenCompraRepository compras;
    private final OrdenTrabajoRepository ordenes;
    private final AsignacionPersonalRepository asignaciones;
    private final AnalisisTrabajoSeguroRepository analisis;
    private final ReferenciasService ref;
    private final UsuarioActualService actual;
    private final CorrelativoDocumentoService correlativos;

    public AsignacionPersonalServiceImpl(OrdenCompraRepository oc, OrdenTrabajoRepository ot,
                                         AsignacionPersonalRepository a, AnalisisTrabajoSeguroRepository ats,
                                         ReferenciasService r, UsuarioActualService u, CorrelativoDocumentoService correlativos) {
        compras = oc;
        ordenes = ot;
        asignaciones = a;
        analisis = ats;
        ref = r;
        actual = u;
        this.correlativos = correlativos;
    }

    private void establecer(OrdenTrabajo ot, Long tecnicoId, Long ssomaId, Long apoyoId, List<Long> extras) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(tecnicoId);
        ids.add(ssomaId);
        if (apoyoId != null) ids.add(apoyoId);
        if (extras != null) ids.addAll(extras);
        int esperados = 2 + (apoyoId == null ? 0 : 1) + (extras == null ? 0 : extras.size());
        if (ids.size() != esperados) throw NegocioException.invalido("No asigne el mismo empleado a dos funciones");

        if (ot.getFechaProgramadaInicio() != null && ot.getFechaProgramadaFin() != null) {
            for (var otra : ordenes.findAll()) {
                if (Objects.equals(otra.getId(), ot.getId()) || "CANCELADA".equals(otra.getEstado()) || otra.getFechaProgramadaInicio() == null || otra.getFechaProgramadaFin() == null ||
                        !ot.getFechaProgramadaInicio().isBefore(otra.getFechaProgramadaFin()) ||
                        !ot.getFechaProgramadaFin().isAfter(otra.getFechaProgramadaInicio())) continue;
                if (asignaciones.findByOrdenTrabajoIdAndEstado(otra.getId(), "A").stream()
                        .anyMatch(a -> ids.contains(a.getEmpleado().getId())))
                    throw NegocioException.conflicto("Personal ya programado en la OT " + otra.getNumeroOrdenTrabajo());
            }
        }
        List<AsignacionPersonal> anteriores = asignaciones.findByOrdenTrabajoId(ot.getId());
        asignaciones.deleteAll(anteriores);
        asignaciones.flush();
        asignar(ot, ref.empleado(tecnicoId, "TECNICO"), "TECNICO");
        if (extras != null) for (Long id : extras) asignar(ot, ref.empleado(id, "TECNICO"), "TECNICO");
        asignar(ot, ref.empleado(ssomaId, "SSOMA"), "SSOMA");
        if (apoyoId != null) asignar(ot, ref.empleado(apoyoId, "APOYO"), "APOYO");
    }

    private void asignar(OrdenTrabajo ot, Empleado e, String rol) {
        AsignacionPersonal a = new AsignacionPersonal();
        a.setOrdenTrabajo(ot);
        a.setEmpleado(e);
        a.setFuncionServicio(rol);
        asignaciones.save(a);
    }

    @Override
    public OrdenTrabajo registrar(AsignacionPersonalRequestDTO f) {
        SolicitudServicio sol = ref.solicitud(f.solicitudId());
        var posibles = compras.findAll().stream().filter(oc ->
                oc.getCotizacion().getSolicitudServicio().getId().equals(sol.getId()) && "REGISTRADA".equals(oc.getEstado())).toList();
        if (posibles.isEmpty()) throw NegocioException.conflicto("La solicitud requiere una OC registrada");
        if (posibles.size() > 1)
            throw NegocioException.conflicto("Hay varias OC; seleccione una en una futura revisión del formulario");
        OrdenCompra oc = posibles.get(0);
        if (ordenes.existsByOrdenCompraId(oc.getId()))
            throw NegocioException.conflicto("Ya existe una OT para esta OC");
        OrdenTrabajo ot = new OrdenTrabajo();
        ot.setOrdenCompra(oc);
        ot.setEmpresaCliente(sol.getEmpresaCliente());
        ot.setEmpleadoRegistra(actual.empleado());
        ot.setNumeroOrdenTrabajo(correlativos.siguiente("ORDEN_TRABAJO", "OT", oc.getFechaEmision().getYear(), 3));
        ot.setEstado("PENDIENTE");
        ordenes.saveAndFlush(ot);
        establecer(ot, f.tecnicoId(), f.ssomaId(), f.apoyoId(), f.tecnicosAdicionales());
        ot.setEstado("ASIGNADA");
        sol.setEstado("ASIGNADA");
        OrdenTrabajo resultado = ordenes.save(ot);
        auditoriaEventos.publishEvent(EventoAuditoria.de("PERSONAL_ASIGNADO", "OrdenTrabajo", resultado.getId(), "Personal asignado al servicio"));
        return resultado;
    }

    @Override
    public OrdenTrabajo cambiarEquipo(Long id, CambioEquipoRequestDTO f) {
        OrdenTrabajo ot = ref.orden(id);
        if (!Set.of("ASIGNADA", "PROGRAMADA").contains(ot.getEstado()) || analisis.existsByOrdenTrabajoId(id))
            throw NegocioException.conflicto("No puede cambiar el equipo después del ATS");
        establecer(ot, f.tecnicoId(), f.ssomaId(), f.apoyoId(), f.tecnicosAdicionales());
        OrdenTrabajo resultado = ordenes.save(ot);
        auditoriaEventos.publishEvent(EventoAuditoria.de("PERSONAL_REASIGNADO", "OrdenTrabajo", resultado.getId(), "Equipo de trabajo modificado"));
        return resultado;
    }
}
