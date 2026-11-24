package pe.edu.sigestec.dto.ejecuciones;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record EjecucionServicioResponseDTO(
        Long id,
        String numeroEjecucion,
        Map<String, Object> ordenTrabajo,
        LocalDateTime fechaEjecucion,
        String descripcion,
        String mediciones,
        String observaciones,
        String estado,
        Map<String, Object> empleadoRegistra,
        List<?> evidencias,
        List<?> materiales
) {}
