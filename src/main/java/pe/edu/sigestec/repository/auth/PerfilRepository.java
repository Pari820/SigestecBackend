package pe.edu.sigestec.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.auth.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
}
