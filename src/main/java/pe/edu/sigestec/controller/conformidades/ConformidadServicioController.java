package pe.edu.sigestec.controller.conformidades;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioRequestDTO;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioResponseDTO;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.conformidades.ConformidadServicioService;

@RestController
@RequestMapping("/api/v1/conformidades")
public class ConformidadServicioController {

    private final ConformidadServicioService service;
    private final RespuestaService respuestas;

    public ConformidadServicioController(ConformidadServicioService service, RespuestaService respuestas) {
        this.service = service;
        this.respuestas = respuestas;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public List<ConformidadServicioResponseDTO> listar() {
        return service.listar().stream().map(respuestas::conformidad).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public ConformidadServicioResponseDTO registrar(
            @Valid @RequestPart("datos") ConformidadServicioRequestDTO datos,
            @RequestPart("acta") MultipartFile acta) {
        return respuestas.conformidad(service.registrar(datos, acta));
    }
}
