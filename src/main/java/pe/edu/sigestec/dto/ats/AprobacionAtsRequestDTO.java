package pe.edu.sigestec.dto.ats;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record AprobacionAtsRequestDTO(@NotBlank String supervisorCliente,
                             @NotBlank String ssomaCliente,
                             @NotNull LocalDate fechaRevision,
                             @NotBlank @Pattern(regexp = "APROBADO|RECHAZADO") String resultado,
                             String observaciones) {}
