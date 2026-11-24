package pe.edu.sigestec.repository.ejecuciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ejecuciones.EvidenciaEjecucion;

public interface EvidenciaEjecucionRepository extends JpaRepository<EvidenciaEjecucion, Long> {
    java.util.List<pe.edu.sigestec.entity.ejecuciones.EvidenciaEjecucion> findByEjecucionServicioId(Long id);
}
