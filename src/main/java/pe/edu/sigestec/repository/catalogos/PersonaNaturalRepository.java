package pe.edu.sigestec.repository.catalogos;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;

public interface PersonaNaturalRepository extends JpaRepository<PersonaNatural, Long> {
    java.util.Optional<PersonaNatural> findByDni(String dni);
}
