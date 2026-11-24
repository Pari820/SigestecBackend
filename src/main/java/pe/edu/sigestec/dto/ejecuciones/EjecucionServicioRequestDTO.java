package pe.edu.sigestec.dto.ejecuciones;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EjecucionServicioRequestDTO(@NotNull Long ordenTrabajoId,
                            @NotBlank String descripcion,
                            String mediciones, String observaciones,
                            List<@Valid MaterialRealDTO> materiales) {}
