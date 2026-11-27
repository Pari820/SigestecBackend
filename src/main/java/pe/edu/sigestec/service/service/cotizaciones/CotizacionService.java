package pe.edu.sigestec.service.service.cotizaciones;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.dto.cotizaciones.CotizacionRequestDTO;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.service.*;

public interface CotizacionService {
    default Cotizacion registrar(CotizacionRequestDTO f) {
        return registrar(f, null);
    }

    Cotizacion registrar(CotizacionRequestDTO f, MultipartFile pdf);

    default Cotizacion editar(Long id, CotizacionRequestDTO f) {
        return editar(id, f, null);
    }

    default Cotizacion editar(Long id, CotizacionRequestDTO f, MultipartFile pdf) {
        return editar(id, f, pdf, false);
    }

    Cotizacion editar(Long id, CotizacionRequestDTO f, MultipartFile pdf, boolean quitarPdf);

    Cotizacion cambiarEstado(Long id, String estado);
    java.util.List<pe.edu.sigestec.entity.cotizaciones.Cotizacion> listar();
}
