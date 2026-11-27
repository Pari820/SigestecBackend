package pe.edu.sigestec.repository.ejecuciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado;

public interface MaterialUtilizadoRepository extends JpaRepository<MaterialUtilizado, Long> {
    java.util.List<pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado> findByEjecucionServicioId(Long id);
    @org.springframework.data.jpa.repository.Query("select mu from MaterialUtilizado mu where mu.ejecucionServicio.ordenTrabajo.id = :ot and mu.material.id = :material")
    java.util.List<pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado> findByOtYMaterial(
        @org.springframework.data.repository.query.Param("ot") Long ot,
        @org.springframework.data.repository.query.Param("material") Long material);
}
