package pe.edu.sigestec.service.service.catalogos;

import java.util.List;
import pe.edu.sigestec.dto.catalogos.ClienteRequestDTO;
import pe.edu.sigestec.dto.catalogos.TipoServicioRequestDTO;
import pe.edu.sigestec.dto.personal.PersonalRequestDTO;
import pe.edu.sigestec.service.*;

public interface CatalogosService {
    List<?> clientes();
    Object cliente(ClienteRequestDTO datos);
    List<?> tipos();
    Object tipo(TipoServicioRequestDTO datos);
    List<?> personal();
    Object personal(PersonalRequestDTO datos);
}
