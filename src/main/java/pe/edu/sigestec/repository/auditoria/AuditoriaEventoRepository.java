package pe.edu.sigestec.repository.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.edu.sigestec.entity.auditoria.AuditoriaEvento;

public interface AuditoriaEventoRepository
        extends JpaRepository<AuditoriaEvento, Long>, JpaSpecificationExecutor<AuditoriaEvento> {}
