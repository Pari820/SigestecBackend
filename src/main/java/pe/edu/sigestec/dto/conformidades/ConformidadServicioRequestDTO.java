package pe.edu.sigestec.dto.conformidades;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record ConformidadServicioRequestDTO(@NotNull Long ordenTrabajoId,
                             @NotNull LocalDate fechaConformidad,
                             @NotBlank String nombreRepresentante,
                             Long representanteEmpresaId,
                             @NotNull @Min(1) @Max(5) Integer valoracion,
                             @NotBlank @Pattern(regexp = "CONFORME|OBSERVADA") String estadoConformidad,
                             String observaciones) {}
