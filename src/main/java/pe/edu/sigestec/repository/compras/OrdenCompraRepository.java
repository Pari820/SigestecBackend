package pe.edu.sigestec.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.compras.OrdenCompra;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    boolean existsByCotizacionId(Long id);

    java.util.Optional<OrdenCompra> findByCotizacionId(Long id);

    boolean existsByNumeroOrdenCompraIgnoreCase(String numero);
    java.util.List<OrdenCompra> findAllByOrderByFechaEmisionAscIdAsc();
}
