package pe.edu.sigestec.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
        if (!configurado()) throw new IllegalStateException("SMTP no configurado");
        try {
            MimeMessage correo = mailSender.createMimeMessage();
            MimeMessageHelper mensaje = new MimeMessageHelper(correo, true, "UTF-8");
            mensaje.setFrom(remitente, nombreRemitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("SIGESTEC | Restablecer contraseña");
            String enlace = escapar(url);
            String texto = "Recibimos una solicitud para restablecer tu contraseña de SIGESTEC.\n"
                    + "Abre este enlace (válido por " + minutos + " minutos y solo una vez):\n" + url
                    + "\nSi no solicitaste este cambio, ignora este mensaje.";
            String html = """
                <!doctype html><html lang="es"><head><meta charset="UTF-8"></head>
                <body style="margin:0;background:#f2f7f8;font-family:Arial,sans-serif;color:#1f333b">
                <div style="max-width:520px;margin:32px auto;background:#fff;border-radius:16px;overflow:hidden;border:1px solid #d8e7e9">
                  <div style="background:#006773;color:#fff;padding:26px 32px;font-size:22px;font-weight:bold">SIGESTEC</div>
                  <div style="padding:32px"><h1 style="font-size:23px;margin:0 0 14px">Restablecer contraseña</h1>
                    <p style="line-height:1.7">Recibimos una solicitud para cambiar tu contraseña de acceso.</p>
                    <p style="margin:26px 0"><a href="%s" style="background:#006773;color:white;padding:14px 22px;border-radius:8px;text-decoration:none;font-weight:bold;display:inline-block">Crear nueva contraseña</a></p>
                    <p style="font-size:14px;line-height:1.7">El enlace estará disponible por <strong>%d minutos</strong> y solo podrá utilizarse una vez.</p>
                    <p style="font-size:13px;color:#53666d">Si no solicitaste este cambio, puedes ignorar este correo.</p>
                  </div><div style="border-top:1px solid #e6eeee;padding:18px 32px;color:#718287;font-size:12px">ACOIEL SAC · SIGESTEC</div>
                </div></body></html>
                """.formatted(enlace, minutos);
            mensaje.setText(texto, html);
            mailSender.send(correo);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("No se pudo construir el correo de recuperación", e);
        }
    }

    private static String escapar(String entrada) {
        return entrada.replace("&", "&amp;").replace("\"", "&quot;")
                .replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }
}
