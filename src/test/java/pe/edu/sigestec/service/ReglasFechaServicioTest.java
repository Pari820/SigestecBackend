package pe.edu.sigestec.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReglasFechaServicioTest {
    @Test
    void atsPermiteRegistroHastaDiaAnteriorInicioOt() {
        LocalDate registro = LocalDate.of(2026, 11, 23);
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 29, 12, 4);
        assertEquals(LocalDate.of(2026, 11, 28), ReglasFechaServicio.ultimaFechaAprobacionAts(inicio));
        assertTrue(ReglasFechaServicio.fechaRevisionValida(registro, inicio, registro));
        assertTrue(ReglasFechaServicio.fechaRevisionValida(registro, inicio, LocalDate.of(2026, 11, 28)));
        assertFalse(ReglasFechaServicio.fechaRevisionValida(registro, inicio, LocalDate.of(2026, 11, 22)));
        assertFalse(ReglasFechaServicio.fechaRevisionValida(registro, inicio, LocalDate.of(2026, 11, 29)));
    }

    @Test
    void informeSoloDesdeDiaSiguienteAlFinDeOtYPermiteFechaSimuladaFutura() {
        LocalDateTime fin = LocalDateTime.of(2026, 11, 30, 12, 4);
        assertEquals(LocalDate.of(2026, 12, 1), ReglasFechaServicio.primeraFechaInforme(fin));
        assertFalse(ReglasFechaServicio.fechaInformeValida(fin, LocalDate.of(2026, 11, 30)));
        assertTrue(ReglasFechaServicio.fechaInformeValida(fin, LocalDate.of(2026, 12, 1)));
        // Una fecha de envío futura respecto al reloj real se acepta en modo de pruebas.
        assertTrue(ReglasFechaServicio.fechaInformeValida(fin, LocalDate.of(2027, 1, 15)));
    }

    @Test
    void conformidadPermiteMismoDiaDeEnvioYFechasSimuladasFuturas() {
        LocalDate envio = LocalDate.of(2026, 12, 1);
        assertFalse(ReglasFechaServicio.fechaConformidadValida(envio, LocalDate.of(2026, 11, 30)));
        assertTrue(ReglasFechaServicio.fechaConformidadValida(envio, envio));
        assertTrue(ReglasFechaServicio.fechaConformidadValida(envio, LocalDate.of(2026, 12, 3)));
        assertTrue(ReglasFechaServicio.fechaConformidadValida(envio, LocalDate.of(2027, 1, 15)));
    }
}
