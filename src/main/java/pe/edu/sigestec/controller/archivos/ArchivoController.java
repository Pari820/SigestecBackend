package pe.edu.sigestec.controller.archivos;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.storage.ArchivoStorageService;

@RestController
@RequestMapping("/api/v1/archivos")
public class ArchivoController {
    private final ArchivoStorageService storage;

    public ArchivoController(ArchivoStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{nombre:.+}")
    public ResponseEntity<Resource> descargar(@PathVariable String nombre) {
        String url = "/api/v1/archivos/" + nombre;
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(storage.tipoContenido(url)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombre + "\"")
                .body(storage.cargar(url));
    }
}
