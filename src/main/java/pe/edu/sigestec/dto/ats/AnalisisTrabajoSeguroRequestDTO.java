package pe.edu.sigestec.dto.ats;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AnalisisTrabajoSeguroRequestDTO(@NotNull Long ordenTrabajoId,
                          @NotBlank String condicionesArea,
                          @NotEmpty List<@NotBlank String> peligros,
                          @NotEmpty List<@NotBlank String> epp,
                          String herramientas,
                          @NotEmpty List<@Valid AtsEtapaDTO> etapas) {}
