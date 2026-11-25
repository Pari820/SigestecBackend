package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.EquipoProteccionPersonal;

public interface EquipoProteccionPersonalRepository extends JpaRepository<EquipoProteccionPersonal, Long> {
    java.util.Optional<EquipoProteccionPersonal> findByNombreIgnoreCase(String nombre);
}
