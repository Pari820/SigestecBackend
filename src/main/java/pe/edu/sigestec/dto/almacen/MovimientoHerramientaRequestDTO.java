package pe.edu.sigestec.dto.almacen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record MovimientoHerramientaRequestDTO(@NotNull Long herramientaId, Long ordenTrabajoId,
                                        Long empleadoResponsableId,
                                        @NotBlank @Pattern(regexp = "INGRESO|PRESTAMO|DEVOLUCION") String tipoMovimiento,
                                        @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal cantidad,
                                        String estadoDevolucion, String observacion) {}
