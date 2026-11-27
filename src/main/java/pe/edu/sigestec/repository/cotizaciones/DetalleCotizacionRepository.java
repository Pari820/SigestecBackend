package pe.edu.sigestec.repository.cotizaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.cotizaciones.DetalleCotizacion;

public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Long> {
    java.util.List<DetalleCotizacion> findByCotizacionIdOrderByItemAsc(Long id);

    void deleteByCotizacionId(Long id);
}
