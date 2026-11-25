package pe.edu.sigestec.controller.ats;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AprobacionAtsRequestDTO;
import pe.edu.sigestec.service.service.ats.AprobacionAtsService;
import pe.edu.sigestec.service.service.common.RespuestaService;

@RestController
@RequestMapping("/api/v1/ats")
public class AprobacionAtsController {

    private final AprobacionAtsService service;
    private final RespuestaService respuestas;

    public AprobacionAtsController(AprobacionAtsService service, RespuestaService respuestas) {
        this.service = service;
        this.respuestas = respuestas;
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public List<?> pendientes() {
        return service.pendientes().stream().map(respuestas::analisisTrabajoSeguro).toList();
    }

    @GetMapping("/aprobaciones")
    @PreAuthorize("hasAnyRole('SSOMA','SUPERVISOR','ADMIN')")
    public List<?> listar() {
        return service.listar().stream().map(respuestas::aprobacion).toList();
    }

    @PutMapping(value = "/{id}/aprobacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public Object aprobar(@PathVariable Long id,
                          @Valid @RequestPart("datos") AprobacionAtsRequestDTO datos,
                          @RequestPart(value = "acta", required = false) MultipartFile acta) {
        return respuestas.aprobacion(service.registrar(id, datos, acta));
    }
}
