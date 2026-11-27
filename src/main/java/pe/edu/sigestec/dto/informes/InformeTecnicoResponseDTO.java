package pe.edu.sigestec.dto.informes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record InformeTecnicoResponseDTO(
        Long id,
        Map<String, Object> ordenTrabajo,
        Long ejecucionId,
        String numeroInforme,
        LocalDate fechaInforme,
        LocalDate fechaEnvio,
        String diagnostico,
        String trabajosRealizados,
        String conclusiones,
        String recomendaciones,
        String estado,
        String urlArchivo,
        Map<String, Object> empleadoRegistra,
        List<?> evidencias
) {}
