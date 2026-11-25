package pe.edu.sigestec.service.service.almacen;

import pe.edu.sigestec.dto.almacen.HerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;
import pe.edu.sigestec.service.*;

public interface MovimientoHerramientaEquipoService {
    HerramientaEquipo crear(HerramientaRequestDTO datos);
    MovimientoHerramientaEquipo registrar(MovimientoHerramientaRequestDTO datos);
    java.util.List<pe.edu.sigestec.entity.almacen.HerramientaEquipo> listarCatalogo();
    java.util.List<pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo> listarMovimientos();
    MovimientoHerramientaEquipo registrarPrestamo(pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO datos);
    MovimientoHerramientaEquipo registrarDevolucion(pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO datos);
}
