package pe.edu.sigestec.controller.gestion;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.service.GestionSeguraService;

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
    @PutMapping(value = "/{modulo}/{id}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,String> corregirConDocumento(@PathVariable String modulo, @PathVariable Long id,
                   @RequestPart("datos") Map<String,Object> datos,
                   @RequestPart(value="pdf", required=false) MultipartFile pdf,
                   @RequestParam(value="quitar", defaultValue="false") boolean quitar) {
        return gestion.editarConDocumento(modulo, id, datos, pdf, quitar);
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
