package pe.edu.sigestec.dto.ats;

import jakarta.validation.constraints.NotBlank;

public record AtsEtapaDTO(@NotBlank String etapa, @NotBlank String peligro,
                           @NotBlank String riesgo, @NotBlank String nivel,
                           @NotBlank String medidaControl) {}
