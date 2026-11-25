package pe.edu.sigestec.dto.almacen;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record HerramientaRequestDTO(String codigo, @NotBlank String nombre,
                                     String descripcion,
                                     @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal cantidadInicial) {}
