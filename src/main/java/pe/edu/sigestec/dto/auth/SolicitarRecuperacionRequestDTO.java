package pe.edu.sigestec.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitarRecuperacionRequestDTO(@NotBlank @Size(max = 60) String username) {}
