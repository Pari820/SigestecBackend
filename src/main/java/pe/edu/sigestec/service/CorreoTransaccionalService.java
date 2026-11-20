package pe.edu.sigestec.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Envia enlaces de recuperacion por SMTP sin servicios API de terceros. */
@Service
public class CorreoTransaccionalService {
    private final JavaMailSender mailSender;
    private final String username;
    private final String password;
    private final String remitente;
    private final String nombreRemitente;

    public CorreoTransaccionalService(JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${app.correo.smtp.remitente-email:}") String remitente,
            @Value("${app.correo.smtp.remitente-nombre:SIGESTEC}") String nombreRemitente) {
        this.mailSender = mailSender;
        this.username = username;
        this.password = password;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    public boolean configurado() {
        return !username.isBlank() && !password.isBlank() && !remitente.isBlank();
    }

    public void enviarRecuperacion(String destinatario, String url, int minutos) {
        if (!configurado()) {
            throw new IllegalStateException("SMTP no configurado: SMTP_USERNAME y SMTP_PASSWORD");
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        // Gmail suele requerir que el remitente coincida con la cuenta autenticada.
        // No se permite cambiar el destino desde el frontend: lo selecciona RecuperacionClaveService.
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("SIGESTEC - recuperación de contraseña");
        mensaje.setText("Se solicitó restablecer una contraseña de SIGESTEC.\n\n" +
                "Abre este enlace para crear una contraseña nueva (válido " + minutos + " minutos):\n" +
                url + "\n\n" +
                "Si no solicitaste esto, ignora el mensaje.\n\n" +
                "Enviado por " + nombreRemitente + ".");
        mailSender.send(mensaje);
    }
}
