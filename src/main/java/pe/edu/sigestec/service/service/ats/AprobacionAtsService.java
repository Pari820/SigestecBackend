package pe.edu.sigestec.service.service.ats;

import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.ats.AprobacionAtsRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.AprobacionAts;
import pe.edu.sigestec.service.*;

public interface AprobacionAtsService {
    AprobacionAts registrar(Long idAts, AprobacionAtsRequestDTO datos, MultipartFile acta);
    java.util.List<pe.edu.sigestec.entity.ats.AprobacionAts> listar();
    java.util.List<pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro> pendientes();
}
