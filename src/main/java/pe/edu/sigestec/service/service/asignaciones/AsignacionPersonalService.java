package pe.edu.sigestec.service.service.asignaciones;

import java.math.*;
import java.time.*;
import java.util.*;
import pe.edu.sigestec.dto.asignaciones.AsignacionPersonalRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.orden_trabajo.CambioEquipoRequestDTO;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.service.*;

public interface AsignacionPersonalService {
    OrdenTrabajo registrar(AsignacionPersonalRequestDTO f);

    OrdenTrabajo cambiarEquipo(Long id, CambioEquipoRequestDTO f);
}
