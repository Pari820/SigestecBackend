package pe.edu.sigestec;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pe.edu.sigestec.service.CorreoTransaccionalService;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorreoTransaccionalServiceTest {
    @Test
    void generaMensajeDeRecuperacionConDestinatarioYEnlace() {
        JavaMailSender sender = mock(JavaMailSender.class);
        CorreoTransaccionalService service = new CorreoTransaccionalService(sender,
                "sigestec835@gmail.com", "contrasena-app-de-prueba", "sigestec835@gmail.com", "SIGESTEC");
        String enlace = "http://localhost:4200/restablecer-clave?token=token-de-prueba";

        assertTrue(service.configurado());
        service.enviarRecuperacion("sigestec835@gmail.com", enlace, 15);

        ArgumentCaptor<SimpleMailMessage> captura = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captura.capture());
        SimpleMailMessage mensaje = captura.getValue();
        assertEquals("sigestec835@gmail.com", mensaje.getFrom());
        assertArrayEquals(new String[]{"sigestec835@gmail.com"}, mensaje.getTo());
        assertTrue(mensaje.getText().contains(enlace));
        assertTrue(mensaje.getText().contains("15 minutos"));
    }

    @Test
    void noEnviaSiFaltanCredencialesSmtp() {
        JavaMailSender sender = mock(JavaMailSender.class);
        CorreoTransaccionalService service = new CorreoTransaccionalService(sender,
                "sigestec835@gmail.com", "", "sigestec835@gmail.com", "SIGESTEC");
        assertFalse(service.configurado());
        assertThrows(IllegalStateException.class,
                () -> service.enviarRecuperacion("sigestec835@gmail.com", "http://localhost:4200/", 15));
        verifyNoInteractions(sender);
    }
}
