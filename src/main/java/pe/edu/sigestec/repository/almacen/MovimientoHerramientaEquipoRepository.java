package pe.edu.sigestec.repository.almacen;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;

public interface MovimientoHerramientaEquipoRepository extends JpaRepository<MovimientoHerramientaEquipo, Long> {
    java.util.List<pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo> findByOrdenTrabajoIdAndHerramientaEquipoIdAndEmpleadoResponsableId(Long ot,Long herramienta,Long empleado);
}
