package pe.edu.sigestec.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.auth.UsuarioRol;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {
    java.util.List<UsuarioRol> findByUsuarioId(Long id);
}
