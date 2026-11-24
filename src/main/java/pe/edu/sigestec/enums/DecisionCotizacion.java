package pe.edu.sigestec.enums;

/** Valores admitidos por la operación de aprobación/rechazo de cotizaciones. */
public enum DecisionCotizacion {
    ACEPTADA, RECHAZADA;

    public static boolean permite(String estado) {
        return ACEPTADA.name().equals(estado) || RECHAZADA.name().equals(estado);
    }
}
