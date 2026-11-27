package pe.edu.sigestec.repository.orden_trabajo;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {
    boolean existsByOrdenCompraId(Long id);

    java.util.Optional<OrdenTrabajo> findByOrdenCompraId(Long id);
    java.util.List<OrdenTrabajo> findAllByOrderByIdAsc();
}
