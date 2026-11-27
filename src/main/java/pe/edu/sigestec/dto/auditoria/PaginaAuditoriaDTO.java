package pe.edu.sigestec.dto.auditoria;

import java.util.List;

public record PaginaAuditoriaDTO(List<AuditoriaEventoDTO> contenido, int pagina,
                                int tamanio, long totalElementos, int totalPaginas,
                                boolean ultima) {}
