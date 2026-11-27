package pe.edu.sigestec.repository.compras;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.sigestec.entity.compras.DetalleOrdenCompra;

public interface DetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {
    java.util.List<DetalleOrdenCompra> findByOrdenCompraIdOrderByItemAsc(Long id);
}
