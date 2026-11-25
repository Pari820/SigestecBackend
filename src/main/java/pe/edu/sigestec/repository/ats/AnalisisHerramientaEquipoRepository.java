package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.AnalisisHerramientaEquipo;

public interface AnalisisHerramientaEquipoRepository extends JpaRepository<AnalisisHerramientaEquipo, Long> {
    java.util.List<AnalisisHerramientaEquipo> findByAnalisisTrabajoSeguroId(Long id);
}
