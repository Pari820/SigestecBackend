package pe.edu.sigestec.repository.informes;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.informes.InformeTecnico;

public interface InformeTecnicoRepository extends JpaRepository<InformeTecnico, Long> {
    boolean existsByOrdenTrabajoId(Long id);
    java.util.Optional<pe.edu.sigestec.entity.informes.InformeTecnico> findByOrdenTrabajoId(Long id);
    java.util.List<InformeTecnico> findAllByOrderByFechaInformeAscIdAsc();
}
