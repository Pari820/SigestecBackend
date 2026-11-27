package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.Riesgo;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {
    java.util.Optional<Riesgo> findByNombreIgnoreCase(String nombre);
}
