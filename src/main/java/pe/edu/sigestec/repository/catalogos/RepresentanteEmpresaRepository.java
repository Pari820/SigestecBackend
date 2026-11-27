package pe.edu.sigestec.repository.catalogos;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;

public interface RepresentanteEmpresaRepository extends JpaRepository<RepresentanteEmpresa, Long> {
    java.util.List<RepresentanteEmpresa> findByPersonaJuridicaId(Long id);
}
