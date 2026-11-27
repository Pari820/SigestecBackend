package pe.edu.sigestec.repository.solicitudes;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;

public interface SolicitudServicioRepository extends JpaRepository<SolicitudServicio, Long> {
    java.util.List<SolicitudServicio> findAllByOrderByFechaSolicitudAscIdAsc();
}
