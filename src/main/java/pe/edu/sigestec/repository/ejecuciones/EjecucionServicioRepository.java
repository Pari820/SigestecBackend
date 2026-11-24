package pe.edu.sigestec.repository.ejecuciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;

public interface EjecucionServicioRepository extends JpaRepository<EjecucionServicio, Long> {
    boolean existsByOrdenTrabajoId(Long id);
    java.util.Optional<pe.edu.sigestec.entity.ejecuciones.EjecucionServicio> findByOrdenTrabajoId(Long id);
    java.util.List<EjecucionServicio> findAllByOrderByFechaEjecucionAscIdAsc();
}
