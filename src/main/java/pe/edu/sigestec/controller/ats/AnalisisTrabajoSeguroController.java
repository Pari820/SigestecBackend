package pe.edu.sigestec.controller.ats;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AnalisisTrabajoSeguroRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.ats.AnalisisTrabajoSeguroService;
import pe.edu.sigestec.service.service.common.RespuestaService;

@RestController
@RequestMapping("/api/v1/ats")
public class AnalisisTrabajoSeguroController {
    private final AnalisisTrabajoSeguroService service;
    private final RespuestaService respuestas;
    public AnalisisTrabajoSeguroController(AnalisisTrabajoSeguroService s, RespuestaService respuestas) {
        service = s;
        this.respuestas = respuestas;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('SSOMA','SUPERVISOR','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::analisisTrabajoSeguro).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SSOMA','ADMIN')")
    public Object elaborar(@Valid @RequestBody AnalisisTrabajoSeguroRequestDTO f) {
        return respuestas.analisisTrabajoSeguro(service.elaborar(f));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SSOMA','ADMIN')")
    public Object elaborarAdjunto(@Valid @RequestPart("datos") AnalisisTrabajoSeguroRequestDTO f,
                                  @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        return respuestas.analisisTrabajoSeguro(service.elaborar(f, pdf));
    }
}
