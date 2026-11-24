package pe.edu.sigestec.service.service.solicitudes;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.solicitudes.SolicitudServicioRequestDTO;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.service.*;

public interface SolicitudServicioService {
    default SolicitudServicio registrar(SolicitudServicioRequestDTO f) {
        return registrar(f, null);
    }

    SolicitudServicio registrar(SolicitudServicioRequestDTO f, MultipartFile pdf);
    java.util.List<pe.edu.sigestec.entity.solicitudes.SolicitudServicio> listar();
}
