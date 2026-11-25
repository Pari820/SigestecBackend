package pe.edu.sigestec.service.service.almacen;

import pe.edu.sigestec.dto.almacen.MaterialRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.service.*;

public interface MovimientoMaterialAlmacenService {
    Material crear(MaterialRequestDTO datos);
    MovimientoMaterialAlmacen registrar(MovimientoMaterialRequestDTO datos);
    java.util.List<pe.edu.sigestec.entity.almacen.Material> listarCatalogo();
    java.util.List<pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen> listarMovimientos();
    pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen registrarDevolucion(pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO datos);
}
