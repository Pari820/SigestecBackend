package pe.edu.sigestec.controller.orden_trabajo;

import java.time.*;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.entity.orden_trabajo.DocumentoPlanificacion;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.orden_trabajo.OrdenTrabajoService;
import pe.edu.sigestec.storage.ArchivoStorageService;

@RestController
@RequestMapping("/api/v1/ordenes-trabajo")
public class OrdenTrabajoController {
    private final OrdenTrabajoService service;
    private final RespuestaService respuestas;
    private final ArchivoStorageService storage;
    public OrdenTrabajoController(OrdenTrabajoService s, RespuestaService respuestas, ArchivoStorageService st) {
        service = s;
        this.respuestas = respuestas;
        storage = st;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','SSOMA','ALMACENERO','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::orden).toList();
    }

    @PostMapping(value = "/{id}/programacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public Object programar(@PathVariable Long id,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
                            @RequestParam String lugar,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEnvio,
                            @RequestParam boolean aprobadoCliente,
                            @RequestPart(value = "pt", required = false) MultipartFile pt,
                            @RequestPart(value = "iper", required = false) MultipartFile iper,
                            @RequestPart(value = "croquis", required = false) MultipartFile croquis,
                            @RequestParam(required = false) String motivoCambio) {
        return respuestas.orden(service.programar(id, inicio, fin, lugar, fechaEnvio, aprobadoCliente, pt, iper, croquis, motivoCambio));
    }

    @GetMapping("/{id}/documentos/{tipo}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','SSOMA','ADMIN')")
    public ResponseEntity<Resource> documento(@PathVariable Long id, @PathVariable String tipo) {
        DocumentoPlanificacion d = service.documento(id, tipo);
        String nombre = d.getNombreDocumento().replaceAll("[\\r\\n\"]", "_");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(storage.tipoContenido(d.getUrlArchivo())))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .body(storage.cargar(d.getUrlArchivo()));
    }
}
