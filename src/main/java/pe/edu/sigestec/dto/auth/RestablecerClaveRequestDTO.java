package pe.edu.sigestec.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerClaveRequestDTO(@NotBlank String token,
                                  @NotBlank @Size(min = 8, max = 72) String claveNueva) {}
