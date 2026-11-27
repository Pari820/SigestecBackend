package pe.edu.sigestec.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.auth.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {
    java.util.Optional<Rol> findByNombre(String nombre);
}
