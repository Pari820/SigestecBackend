package pe.edu.sigestec.dto.solicitudes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudServicioRequestDTO(@NotNull Long clienteId, @NotNull Long tipoServicioId,
                                @NotBlank String descripcion, String observaciones,
                                Long representanteId, String lugarServicio) {}
