package pe.edu.sigestec.repository.ats;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ats.DetalleAnalisisRiesgo;

public interface DetalleAnalisisRiesgoRepository extends JpaRepository<DetalleAnalisisRiesgo, Long> {
    java.util.List<DetalleAnalisisRiesgo> findByAnalisisTrabajoSeguroId(Long id);
}
