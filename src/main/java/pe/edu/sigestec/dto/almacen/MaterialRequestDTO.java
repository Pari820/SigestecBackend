package pe.edu.sigestec.dto.almacen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MaterialRequestDTO(String codigo, @NotBlank String nombre,
                                   @NotBlank String unidadMedida,
                                   @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal stockInicial,
                                   @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal stockMinimo) {}
