package pe.edu.sigestec.service.service.compras;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.compras.DetalleOrdenCompraRequestDTO;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.service.*;

public interface OrdenCompraService {
    OrdenCompra registrar(Long cotizacionId, String numero, LocalDate fecha, BigDecimal monto, MultipartFile pdf, List<DetalleOrdenCompraRequestDTO> detalles);
    java.util.List<pe.edu.sigestec.entity.compras.OrdenCompra> listar();
    pe.edu.sigestec.entity.compras.OrdenCompra buscarPorId(Long id);
}
