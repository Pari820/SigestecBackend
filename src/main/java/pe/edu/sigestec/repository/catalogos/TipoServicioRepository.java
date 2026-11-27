package pe.edu.sigestec.repository.catalogos;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.catalogos.TipoServicio;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    java.util.Optional<TipoServicio> findByNombreIgnoreCase(String nombre);
}
