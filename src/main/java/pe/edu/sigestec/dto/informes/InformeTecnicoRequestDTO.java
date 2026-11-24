package pe.edu.sigestec.dto.informes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record InformeTecnicoRequestDTO(@NotNull Long ordenTrabajoId, String numeroInforme,
                          @NotBlank String diagnostico,
                          @NotBlank String trabajosRealizados,
                          @NotBlank String conclusiones,
                          String recomendaciones,
                          @NotNull LocalDate fechaEnvio) {}
