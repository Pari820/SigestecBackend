package pe.edu.sigestec.service.service.auditoria;

import java.time.LocalDate;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import pe.edu.sigestec.dto.auditoria.PaginaAuditoriaDTO;

public interface AuditoriaService {
    void registrar(EventoAuditoria evento);
    PaginaAuditoriaDTO buscar(String usuario, String accion, LocalDate desde,
                              LocalDate hasta, int pagina, int tamanio);
}
