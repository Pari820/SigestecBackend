package pe.edu.sigestec.repository.almacen;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.almacen.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    java.util.Optional<pe.edu.sigestec.entity.almacen.Material> findByCodigoIgnoreCase(String codigo);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Material m where m.id = :id")
    java.util.Optional<pe.edu.sigestec.entity.almacen.Material> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") Long id);
    java.util.List<Material> findAllByOrderByIdAsc();
}
