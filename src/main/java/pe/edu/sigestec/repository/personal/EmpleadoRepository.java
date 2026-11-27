package pe.edu.sigestec.repository.personal;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.personal.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    java.util.Optional<Empleado> findByPersonaNaturalId(Long id);
}
