package pe.edu.sigestec.controller.gestion;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO;
import pe.edu.sigestec.service.service.almacen.MovimientoHerramientaEquipoService;
import pe.edu.sigestec.service.service.common.RespuestaService;

@RestController
@RequestMapping("/api/v1/herramientas")
@PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
public class HerramientaOperacionController {

    private final MovimientoHerramientaEquipoService service;
    private final RespuestaService respuestas;

    public HerramientaOperacionController(MovimientoHerramientaEquipoService service,
                                         RespuestaService respuestas) {
        this.service = service;
        this.respuestas = respuestas;
    }

    @PostMapping("/prestamos")
    public Object prestamo(@Valid @RequestBody MovimientoHerramientaRequestDTO datos) {
        return respuestas.movimientoHerramienta(service.registrarPrestamo(datos));
    }

    @PostMapping("/devoluciones")
    public Object devolucion(@Valid @RequestBody MovimientoHerramientaRequestDTO datos) {
        return respuestas.movimientoHerramienta(service.registrarDevolucion(datos));
    }
}
