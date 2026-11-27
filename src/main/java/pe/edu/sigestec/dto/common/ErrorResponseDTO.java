package pe.edu.sigestec.dto.common;

import java.util.Map;

/** Contrato de error uniforme; mantiene "mensaje" para el frontend existente. */
public record ErrorResponseDTO(
        String fecha,
        int status,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> erroresValidacion,
        String requestId
) {
}
