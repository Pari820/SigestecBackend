package pe.edu.sigestec.dto.personal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonalRequestDTO(
            @NotBlank @Size(max = 220) String nombre,
            @NotBlank String perfil,
            @Pattern(regexp = "(?:[0-9]{8})?", message = "El DNI debe tener exactamente 8 dígitos") String dni,
            @Size(max = 100) String nombres,
            @Size(max = 120) String apellidos) {}
