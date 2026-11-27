package pe.edu.sigestec.dto.catalogos;

import jakarta.validation.constraints.NotBlank;

public record TipoServicioRequestDTO(@NotBlank String nombre, String descripcion) {}
