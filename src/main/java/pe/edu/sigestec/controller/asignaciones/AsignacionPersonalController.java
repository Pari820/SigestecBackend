package pe.edu.sigestec.controller.asignaciones;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.dto.asignaciones.AsignacionPersonalRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.orden_trabajo.CambioEquipoRequestDTO;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.asignaciones.AsignacionPersonalService;
import pe.edu.sigestec.service.service.common.RespuestaService;

@RestController
@RequestMapping("/api/v1/ordenes-trabajo")
public class AsignacionPersonalController {
    private final AsignacionPersonalService service;
    private final RespuestaService respuestas;

    public AsignacionPersonalController(AsignacionPersonalService s, RespuestaService respuestas) {
        service = s;
        this.respuestas = respuestas;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public Object asignar(@Valid @RequestBody AsignacionPersonalRequestDTO f) {
        return respuestas.orden(service.registrar(f));
    }

    @PutMapping("/{id}/equipo")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public Object cambiar(@PathVariable Long id, @Valid @RequestBody CambioEquipoRequestDTO f) {
        return respuestas.orden(service.cambiarEquipo(id, f));
    }
}
