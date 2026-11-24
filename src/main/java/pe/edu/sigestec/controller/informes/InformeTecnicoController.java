package pe.edu.sigestec.controller.informes;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.informes.InformeTecnicoRequestDTO;
import pe.edu.sigestec.dto.informes.InformeTecnicoResponseDTO;
import pe.edu.sigestec.service.service.common.RespuestaService;
import pe.edu.sigestec.service.service.informes.InformeTecnicoService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/informes")
public class InformeTecnicoController {

    private final InformeTecnicoService service;
    private final RespuestaService respuestas;


    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public List<InformeTecnicoResponseDTO> listar() {
        return service.listar().stream().map(respuestas::informe).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public InformeTecnicoResponseDTO registrar(
            @Valid @RequestPart("datos") InformeTecnicoRequestDTO datos,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf,
            @RequestPart(value = "fotos", required = false) List<MultipartFile> fotos) {
        return respuestas.informe(service.registrar(datos, pdf, fotos));
    }
}
