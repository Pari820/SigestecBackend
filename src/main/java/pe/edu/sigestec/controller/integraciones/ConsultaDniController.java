package pe.edu.sigestec.controller.integraciones;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.sigestec.service.ConsultaDniService;

@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaDniController {
    private final ConsultaDniService consulta;

    public ConsultaDniController(ConsultaDniService consulta) {
        this.consulta = consulta;
    }

    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public ConsultaDniService.PersonaDni dni(@PathVariable String dni) {
        return consulta.consultar(dni);
    }
}
