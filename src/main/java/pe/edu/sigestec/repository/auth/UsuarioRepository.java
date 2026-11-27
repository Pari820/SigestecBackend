package pe.edu.sigestec.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.auth.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    java.util.Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}
