
package pe.edu.sigestec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import pe.edu.sigestec.service.CorreoTransaccionalService;

class CorreoTransaccionalServiceTest {

    @Test
    void enviaCorreoHtmlConDestinatario() throws Exception {
        JavaMailSender sender = mock(JavaMailSender.class);

        MimeMessage mime = new MimeMessage(
            Session.getInstance(new Properties())
        );

        when(sender.createMimeMessage()).thenReturn(mime);

        CorreoTransaccionalService service =
            new CorreoTransaccionalService(
                sender,
                "prueba@example.org",
                "clave-de-aplicacion-de-prueba",
                "prueba@example.org",
                "SIGESTEC"
            );

        assertTrue(service.configurado());

        service.enviarRecuperacion(
            "destino@example.org",
            "http://localhost:4200/restablecer-clave?token=prueba",
            15
        );

        ArgumentCaptor<MimeMessage> captura =
            ArgumentCaptor.forClass(MimeMessage.class);

        verify(sender).send(captura.capture());

        MimeMessage mensaje = captura.getValue();

        mensaje.saveChanges();

        assertEquals(
            "SIGESTEC | Restablecer contraseña",
            mensaje.getSubject()
        );

        assertEquals(
            "destino@example.org",
            mensaje.getRecipients(Message.RecipientType.TO)[0].toString()
        );

        assertTrue(
            mensaje.getContentType().toLowerCase().contains("multipart"),
            "El correo debería tener contenido multipart, pero se obtuvo: "
                + mensaje.getContentType()
        );
    }

    @Test
    void noEnviaSiFaltanCredencialesSmtp() {
        JavaMailSender sender = mock(JavaMailSender.class);

        CorreoTransaccionalService service =
            new CorreoTransaccionalService(
                sender,
                "prueba@example.org",
                "",
                "prueba@example.org",
                "SIGESTEC"
            );

        assertFalse(service.configurado());

        assertThrows(
            IllegalStateException.class,
            () -> service.enviarRecuperacion(
                "destino@example.org",
                "http://localhost:4200/",
                15
            )
        );

        verifyNoInteractions(sender);
    }
}
