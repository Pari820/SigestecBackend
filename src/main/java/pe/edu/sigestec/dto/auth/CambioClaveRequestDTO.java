package pe.edu.sigestec.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioClaveRequestDTO(@NotBlank String claveActual,
                              @NotBlank @Size(min = 8, max = 72) String claveNueva) {}
