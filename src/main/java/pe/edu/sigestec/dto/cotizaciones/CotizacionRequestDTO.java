package pe.edu.sigestec.dto.cotizaciones;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CotizacionRequestDTO(@NotNull Long solicitudId, String descripcionTrabajo,
                                 String observaciones, @NotEmpty List<@Valid DetalleCotizacionRequestDTO> detalles) {}
