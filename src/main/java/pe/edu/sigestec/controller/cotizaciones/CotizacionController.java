package pe.edu.sigestec.controller.cotizaciones;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.cotizaciones.CotizacionRequestDTO;
import pe.edu.sigestec.dto.cotizaciones.EstadoRequestDTO;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.cotizaciones.CotizacionService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cotizaciones")
public class CotizacionController {
    private final CotizacionService service;
    private final RespuestaService respuestas;


    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::cotizacion).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object registrar(@Valid @RequestBody CotizacionRequestDTO f) {
        return respuestas.cotizacion(service.registrar(f));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object registrarAdjunto(@Valid @RequestPart("datos") CotizacionRequestDTO f,
                                   @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        return respuestas.cotizacion(service.registrar(f, pdf));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object editar(@PathVariable Long id, @Valid @RequestBody CotizacionRequestDTO f) {
        return respuestas.cotizacion(service.editar(id, f));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object editarAdjunto(@PathVariable Long id, @Valid @RequestPart("datos") CotizacionRequestDTO f,
                                @RequestPart(value = "pdf", required = false) MultipartFile pdf,
                                @RequestParam(value = "quitarPdf", defaultValue = "false") boolean quitarPdf) {
        return respuestas.cotizacion(service.editar(id, f, pdf, quitarPdf));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object estado(@PathVariable Long id, @Valid @RequestBody EstadoRequestDTO f) {
        return respuestas.cotizacion(service.cambiarEstado(id, f.estado()));
    }
}
