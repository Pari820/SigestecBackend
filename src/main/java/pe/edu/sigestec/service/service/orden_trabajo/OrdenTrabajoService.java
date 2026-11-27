package pe.edu.sigestec.service.service.orden_trabajo;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.orden_trabajo.DocumentoPlanificacion;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.service.*;

public interface OrdenTrabajoService {
    OrdenTrabajo programar(Long id, LocalDateTime inicio, LocalDateTime fin, String lugar, LocalDate fechaEnvio, boolean aprobado, MultipartFile pt, MultipartFile iper, MultipartFile croquis, String motivo);

    DocumentoPlanificacion documento(Long id, String tipo);
    java.util.List<pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo> listar();
}
