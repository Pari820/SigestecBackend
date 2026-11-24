package pe.edu.sigestec.repository.conformidades;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;

public interface ConformidadServicioRepository extends JpaRepository<ConformidadServicio, Long> {
    boolean existsByOrdenTrabajoId(Long id);
}
