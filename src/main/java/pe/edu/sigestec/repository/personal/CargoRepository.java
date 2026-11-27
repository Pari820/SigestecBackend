package pe.edu.sigestec.repository.personal;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.personal.Cargo;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    java.util.Optional<Cargo> findByNombreIgnoreCase(String nombre);
}
