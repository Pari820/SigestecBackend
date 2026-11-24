package pe.edu.sigestec.service.service.conformidades;

import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioRequestDTO;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.service.*;

public interface ConformidadServicioService {
    ConformidadServicio registrar(ConformidadServicioRequestDTO datos, MultipartFile acta);
    java.util.List<pe.edu.sigestec.entity.conformidades.ConformidadServicio> listar();
}
