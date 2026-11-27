package pe.edu.sigestec.controller.solicitudes;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.solicitudes.SolicitudServicioRequestDTO;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.solicitudes.SolicitudServicioService;

@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudServicioController {
    private final SolicitudServicioService service;
    private final RespuestaService respuestas;
    public SolicitudServicioController(SolicitudServicioService s, RespuestaService respuestas) {
        service = s;
        this.respuestas = respuestas;
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','SSOMA','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::solicitud).toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object registrar(@Valid @RequestBody SolicitudServicioRequestDTO f) {
        return respuestas.solicitud(service.registrar(f));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object registrarAdjunto(@Valid @RequestPart("datos") SolicitudServicioRequestDTO f,
                                   @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        return respuestas.solicitud(service.registrar(f, pdf));
    }
}
