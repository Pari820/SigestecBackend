package pe.edu.sigestec.repository.informes;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.informes.EvidenciaInformeTecnico;

public interface EvidenciaInformeTecnicoRepository extends JpaRepository<EvidenciaInformeTecnico, Long> {
    java.util.List<pe.edu.sigestec.entity.informes.EvidenciaInformeTecnico> findByInformeTecnicoId(Long id);
}
