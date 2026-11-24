package pe.edu.sigestec.repository.asignaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.asignaciones.AsignacionPersonal;

public interface AsignacionPersonalRepository extends JpaRepository<AsignacionPersonal, Long> {
    java.util.List<AsignacionPersonal> findByOrdenTrabajoIdAndEstado(Long id, String estado);

    java.util.List<AsignacionPersonal> findByOrdenTrabajoId(Long id);

    void deleteByOrdenTrabajoId(Long id);
}
