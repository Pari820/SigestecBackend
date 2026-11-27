package pe.edu.sigestec.controller.catalogos;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.dto.catalogos.ClienteRequestDTO;
import pe.edu.sigestec.dto.catalogos.TipoServicioRequestDTO;
import pe.edu.sigestec.dto.personal.PersonalRequestDTO;
import pe.edu.sigestec.service.service.catalogos.CatalogosService;

@RestController
@RequestMapping("/api/v1")
public class CatalogosController {
    private final CatalogosService service;

    public CatalogosController(CatalogosService service) {
        this.service = service;
    }

    @GetMapping("/clientes")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public List<?> clientes() {
        return service.clientes();
    }

    @PostMapping("/clientes")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object cliente(@Valid @RequestBody ClienteRequestDTO f) {
        return service.cliente(f);
    }

    @GetMapping("/tipos-servicio")
    @PreAuthorize("hasAnyRole('GERENTE','SUPERVISOR','SSOMA','ADMIN')")
    public List<?> tipos() {
        return service.tipos();
    }

    @PostMapping("/tipos-servicio")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public Object tipo(@Valid @RequestBody TipoServicioRequestDTO f) {
        return service.tipo(f);
    }

    @GetMapping("/personal")
    @PreAuthorize("hasAnyRole('SUPERVISOR','SSOMA','ALMACENERO','ADMIN')")
    public List<?> personal() {
        return service.personal();
    }

    @PostMapping("/personal")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public Object personal(@Valid @RequestBody PersonalRequestDTO f) {
        return service.personal(f);
    }
}
