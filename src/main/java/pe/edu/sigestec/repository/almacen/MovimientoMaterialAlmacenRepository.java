package pe.edu.sigestec.repository.almacen;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;

public interface MovimientoMaterialAlmacenRepository extends JpaRepository<MovimientoMaterialAlmacen, Long> {
    java.util.List<pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen> findByOrdenTrabajoIdAndMaterialId(Long ordenTrabajoId,Long materialId);
}
