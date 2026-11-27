package pe.edu.sigestec.controller.integraciones;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.sigestec.service.ConsultaSunatService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaSunatController {
    private final ConsultaSunatService consulta;


    @GetMapping("/empresas")
    @PreAuthorize("hasAnyRole('GERENTE','ADMIN')")
    public List<ConsultaSunatService.Empresa> empresas(@RequestParam String buscar) {
        return consulta.consultar(buscar);
    }
}
