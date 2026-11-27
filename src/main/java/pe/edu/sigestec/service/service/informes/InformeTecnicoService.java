package pe.edu.sigestec.service.service.informes;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.informes.InformeTecnicoRequestDTO;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.service.*;

public interface InformeTecnicoService {
    InformeTecnico registrar(InformeTecnicoRequestDTO datos, MultipartFile pdf, List<MultipartFile> fotos);
    java.util.List<pe.edu.sigestec.entity.informes.InformeTecnico> listar();
}
