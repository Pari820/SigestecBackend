package pe.edu.sigestec.controller.ejecuciones;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioRequestDTO;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioResponseDTO;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.ejecuciones.EjecucionServicioService;

@RestController
@RequestMapping("/api/v1/ejecuciones")
public class EjecucionServicioController {

    private final EjecucionServicioService service;
    private final RespuestaService respuestas;

    public EjecucionServicioController(EjecucionServicioService service, RespuestaService respuestas) {
        this.service = service;
        this.respuestas = respuestas;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','SSOMA','ADMIN')")
    public List<EjecucionServicioResponseDTO> listar() {
        return service.listar().stream().map(respuestas::ejecucion).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public EjecucionServicioResponseDTO registrar(
            @Valid @RequestPart("datos") EjecucionServicioRequestDTO datos,
            @RequestPart(value = "antes", required = false) MultipartFile antes,
            @RequestPart(value = "durante", required = false) MultipartFile durante,
            @RequestPart(value = "despues", required = false) MultipartFile despues) {
        return respuestas.ejecucion(service.registrar(datos, antes, durante, despues));
    }
}
