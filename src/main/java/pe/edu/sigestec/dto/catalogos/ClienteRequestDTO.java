package pe.edu.sigestec.dto.catalogos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteRequestDTO(@NotBlank @Pattern(regexp = "[0-9]{11}") String ruc,
                              @NotBlank String razonSocial, String telefono, String contacto) {}
