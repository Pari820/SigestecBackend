package pe.edu.sigestec.controller.almacen;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.dto.almacen.HerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MaterialRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO;
import pe.edu.sigestec.service.service.almacen.MovimientoHerramientaEquipoService;
import pe.edu.sigestec.service.service.almacen.MovimientoMaterialAlmacenService;
import pe.edu.sigestec.service.service.common.RespuestaService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/almacen")
public class AlmacenController {

    private final MovimientoMaterialAlmacenService servicioMaterial;
    private final MovimientoHerramientaEquipoService servicioHerramienta;
    private final RespuestaService respuestas;


    @GetMapping("/materiales")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ALMACENERO','ADMIN')")
    public List<?> materiales() {
        return servicioMaterial.listarCatalogo().stream().map(respuestas::material).toList();
    }

    @PostMapping("/materiales")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public Object material(@Valid @RequestBody MaterialRequestDTO datos) {
        return respuestas.material(servicioMaterial.crear(datos));
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public List<?> movimientos() {
        return servicioMaterial.listarMovimientos().stream().map(respuestas::movimientoMaterial).toList();
    }

    @PostMapping("/movimientos")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public Object movimiento(@Valid @RequestBody MovimientoMaterialRequestDTO datos) {
        return respuestas.movimientoMaterial(servicioMaterial.registrar(datos));
    }

    @PostMapping("/devoluciones")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public Object devolucion(@Valid @RequestBody MovimientoMaterialRequestDTO datos) {
        return respuestas.movimientoMaterial(servicioMaterial.registrarDevolucion(datos));
    }

    @GetMapping("/herramientas")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public List<?> herramientas() {
        return servicioHerramienta.listarCatalogo().stream().map(respuestas::herramienta).toList();
    }

    @PostMapping("/herramientas")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public Object herramienta(@Valid @RequestBody HerramientaRequestDTO datos) {
        return respuestas.herramienta(servicioHerramienta.crear(datos));
    }

    @GetMapping("/herramientas/movimientos")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public List<?> movimientosHerramienta() {
        return servicioHerramienta.listarMovimientos().stream().map(respuestas::movimientoHerramienta).toList();
    }

    @PostMapping("/herramientas/movimientos")
    @PreAuthorize("hasAnyRole('ALMACENERO','ADMIN')")
    public Object movimientoHerramienta(@Valid @RequestBody MovimientoHerramientaRequestDTO datos) {
        return respuestas.movimientoHerramienta(servicioHerramienta.registrar(datos));
    }
}
