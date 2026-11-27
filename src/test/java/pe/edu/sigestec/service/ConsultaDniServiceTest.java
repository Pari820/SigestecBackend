package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pe.edu.sigestec.exception.NegocioException;

class ConsultaDniServiceTest {
    private final ConsultaDniService consulta = new ConsultaDniService(new ObjectMapper(), "");

    @Test
    void rechazaDniConLongitudIncorrecta() {
        NegocioException error = assertThrows(NegocioException.class, () -> consulta.consultar("12345"));
        assertTrue(error.getMessage().contains("8 dígitos"));
    }

    @Test
    void requiereClaveDeProveedor() {
        NegocioException error = assertThrows(NegocioException.class, () -> consulta.consultar("12345678"));
        assertTrue(error.getMessage().contains("IDENTIDAD_API_TOKEN"));
    }

    @Test
    void interpretaNombresApellidosSinGuardarDatos() throws Exception {
        String respuesta = """
                {"success":true,"data":{"dni":"12345678","nombres":"ANA LUZ",
                "apellido_paterno":"PEREZ","apellido_materno":"GOMEZ"}}
                """;
        var persona = consulta.interpretar("12345678", respuesta);
        assertEquals("ANA LUZ", persona.nombres());
        assertEquals("PEREZ", persona.apellidoPaterno());
        assertEquals("GOMEZ", persona.apellidoMaterno());
    }

    @Test
    void rechazaRespuestaConDniDiferente() {
        String respuesta = """
                {"success":true,"data":{"dni":"87654321","nombres":"ANA",
                "apellido_paterno":"PEREZ","apellido_materno":"GOMEZ"}}
                """;
        assertThrows(NegocioException.class, () -> consulta.interpretar("12345678", respuesta));
    }
}
