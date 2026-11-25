package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.AnalisisEquipoProteccionPersonal;

public interface AnalisisEquipoProteccionPersonalRepository extends JpaRepository<AnalisisEquipoProteccionPersonal, Long> {
    java.util.List<AnalisisEquipoProteccionPersonal> findByAnalisisTrabajoSeguroId(Long id);
}
