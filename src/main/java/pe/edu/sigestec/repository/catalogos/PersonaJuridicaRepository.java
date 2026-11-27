package pe.edu.sigestec.repository.catalogos;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;

public interface PersonaJuridicaRepository extends JpaRepository<PersonaJuridica, Long> {
    java.util.Optional<PersonaJuridica> findByRuc(String ruc);
}
