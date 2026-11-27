package pe.edu.sigestec.repository.orden_trabajo;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.orden_trabajo.DocumentoPlanificacion;

public interface DocumentoPlanificacionRepository extends JpaRepository<DocumentoPlanificacion, Long> {
    java.util.List<DocumentoPlanificacion> findByOrdenTrabajoId(Long id);

    java.util.Optional<DocumentoPlanificacion> findByOrdenTrabajoIdAndTipoDocumento(Long id, String tipo);
}
