package pe.edu.sigestec.repository.cotizaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    java.util.List<Cotizacion> findBySolicitudServicioId(Long id);

    boolean existsByNumeroCotizacion(String numeroCotizacion);
    java.util.List<Cotizacion> findAllByOrderByFechaEmisionAscIdAsc();
}
