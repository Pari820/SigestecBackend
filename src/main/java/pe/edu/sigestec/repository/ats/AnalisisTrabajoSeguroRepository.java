package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;

public interface AnalisisTrabajoSeguroRepository extends JpaRepository<AnalisisTrabajoSeguro, Long> {
    boolean existsByOrdenTrabajoId(Long id);
    java.util.List<AnalisisTrabajoSeguro> findAllByOrderByFechaRegistroAscIdAsc();
}
