package pe.edu.sigestec.service.service.ejecuciones;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioRequestDTO;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.service.*;

public interface EjecucionServicioService {
    EjecucionServicio registrar(EjecucionServicioRequestDTO datos, MultipartFile antes, MultipartFile durante, MultipartFile despues);
    java.util.List<pe.edu.sigestec.entity.ejecuciones.EjecucionServicio> listar();
}
