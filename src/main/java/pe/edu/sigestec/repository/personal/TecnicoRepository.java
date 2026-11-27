package pe.edu.sigestec.repository.personal;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.personal.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
}
