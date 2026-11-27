package pe.edu.sigestec.dto.ejecuciones;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MaterialRealDTO(@NotNull Long materialId,
                               @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal cantidadReal) {}
