package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.AprobacionAts;

public interface AprobacionAtsRepository extends JpaRepository<AprobacionAts, Long> {
    boolean existsByAnalisisTrabajoSeguroId(Long id);
}
