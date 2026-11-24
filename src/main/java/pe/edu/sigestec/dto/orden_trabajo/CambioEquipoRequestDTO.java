package pe.edu.sigestec.dto.orden_trabajo;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CambioEquipoRequestDTO(@NotNull Long tecnicoId, @NotNull Long ssomaId,
                                   Long apoyoId, List<Long> tecnicosAdicionales) {}
