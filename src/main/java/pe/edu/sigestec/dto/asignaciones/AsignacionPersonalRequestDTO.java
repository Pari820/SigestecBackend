package pe.edu.sigestec.dto.asignaciones;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AsignacionPersonalRequestDTO(@NotNull Long solicitudId, @NotNull Long tecnicoId,
                                 @NotNull Long ssomaId, Long apoyoId, List<Long> tecnicosAdicionales) {}
