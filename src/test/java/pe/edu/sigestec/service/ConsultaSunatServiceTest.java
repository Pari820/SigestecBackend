package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pe.edu.sigestec.exception.NegocioException;

class ConsultaSunatServiceTest {
    private final ConsultaSunatService sinClave =
            new ConsultaSunatService(new ObjectMapper(), "");
    private final ConsultaSunatService conClave =
            new ConsultaSunatService(new ObjectMapper(), "token-prueba");

    @Test
    void exigeConfigurarTokenAntesDeConsultar() {
        NegocioException ex = assertThrows(NegocioException.class,
                () -> sinClave.consultar("20123456789"));
        assertTrue(ex.getMessage().contains("SUNAT_API_TOKEN"));
    }

    @Test
    void rechazaRucNumericoConLongitudIncorrecta() {
        assertThrows(NegocioException.class, () -> conClave.consultar("12345678"));
        assertThrows(NegocioException.class, () -> conClave.consultar("123456789012"));
    }

    @Test
    void rechazaRazonSocialDemasiadoCortaOLarga() {
        assertThrows(NegocioException.class, () -> conClave.consultar("ab"));
        assertThrows(NegocioException.class, () -> conClave.consultar("x".repeat(81)));
    }

    @Test
    void normalizaEspaciosYRechazaEntradaVacia() {
        assertThrows(NegocioException.class, () -> conClave.consultar("   "));
        assertThrows(NegocioException.class, () -> conClave.consultar(null));
    }
}
