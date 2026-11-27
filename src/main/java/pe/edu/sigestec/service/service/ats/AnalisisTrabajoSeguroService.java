package pe.edu.sigestec.service.service.ats;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AnalisisTrabajoSeguroRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.service.*;

public interface AnalisisTrabajoSeguroService {
    default AnalisisTrabajoSeguro elaborar(AnalisisTrabajoSeguroRequestDTO f) {
        return elaborar(f, null);
    }

    AnalisisTrabajoSeguro elaborar(AnalisisTrabajoSeguroRequestDTO f, MultipartFile pdf);
    java.util.List<pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro> listar();
}
