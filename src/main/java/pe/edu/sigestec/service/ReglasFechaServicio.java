package pe.edu.sigestec.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Reglas de límites de fecha del flujo ATS -> OT -> informe -> conformidad. */
public final class ReglasFechaServicio {
    private ReglasFechaServicio() { }

    public static LocalDate ultimaFechaAprobacionAts(LocalDateTime inicioOt) {
        return inicioOt.toLocalDate().minusDays(1);
    }

    public static boolean fechaRevisionValida(LocalDate registroAts, LocalDateTime inicioOt, LocalDate revision) {
        return registroAts != null && inicioOt != null && revision != null
            && !revision.isBefore(registroAts)
            && !revision.isAfter(ultimaFechaAprobacionAts(inicioOt));
    }

    public static LocalDate primeraFechaInforme(LocalDateTime finOt) {
        return finOt.toLocalDate().plusDays(1);
    }

    /** Permite fechas simuladas futuras; conserva el orden cronológico del flujo. */
    public static boolean fechaInformeValida(LocalDateTime finOt, LocalDate fechaEnvio) {
        return finOt != null && fechaEnvio != null
            && !fechaEnvio.isBefore(primeraFechaInforme(finOt));
    }

    /** La conformidad debe ser el mismo día del envío del informe o posterior. */
    public static boolean fechaConformidadValida(LocalDate envioInforme, LocalDate conformidad) {
        return envioInforme != null && conformidad != null
            && !conformidad.isBefore(envioInforme);
    }
}
