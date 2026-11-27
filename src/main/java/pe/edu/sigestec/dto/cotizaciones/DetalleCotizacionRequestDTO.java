package pe.edu.sigestec.dto.cotizaciones;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DetalleCotizacionRequestDTO(@NotBlank String descripcion,
                                        @NotNull @DecimalMin("0.01") BigDecimal cantidad,
                                        @NotNull @DecimalMin("0.00") BigDecimal precioUnitario,
                                        String unidadMedida) {}
