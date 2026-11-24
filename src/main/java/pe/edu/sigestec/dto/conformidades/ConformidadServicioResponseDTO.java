package pe.edu.sigestec.dto.conformidades;

import java.time.LocalDate;
import java.util.Map;

public record ConformidadServicioResponseDTO(
        Long id,
        Map<String, Object> ordenTrabajo,
        Long informeId,
        String numeroActa,
        LocalDate fechaConformidad,
        String nombreRepresentante,
        Integer valoracion,
        String estadoConformidad,
        String observaciones,
        String urlArchivo,
        Long representanteEmpresaId,
        Map<String, Object> empleadoRegistra
) {}
