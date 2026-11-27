package pe.edu.sigestec.dto.cotizaciones;

import jakarta.validation.constraints.NotBlank;

public record EstadoRequestDTO(@NotBlank String estado) {}
