package pe.edu.sigestec.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.service.GestionSeguraService;
import java.util.Map;

/** Endpoints acotados: el servicio verifica las reglas y los roles, con errores 403/409 explicativos. */
@RestController
@RequestMapping("/api/v1/gestion")
public class GestionSeguraController {
    private final GestionSeguraService gestion;
    public GestionSeguraController(GestionSeguraService gestion) { this.gestion = gestion; }

    @PutMapping("/{modulo}/{id}")
    public Map<String,String> corregir(@PathVariable String modulo, @PathVariable Long id,
                                        @RequestBody Map<String,Object> datos) {
        return gestion.editar(modulo, id, datos);
    }
    @PatchMapping("/{modulo}/{id}/cancelar")
    public Map<String,String> cancelar(@PathVariable String modulo, @PathVariable Long id) {
        return gestion.retirar(modulo, id, "cancelar");
    }
    @PatchMapping("/{modulo}/{id}/reactivar")
    public Map<String,String> reactivar(@PathVariable String modulo, @PathVariable Long id) {
        return gestion.reactivar(modulo, id);
    }
    @DeleteMapping("/{modulo}/{id}")
    public Map<String,String> eliminar(@PathVariable String modulo, @PathVariable Long id) {
        return gestion.retirar(modulo, id, "eliminar");
    }
}
