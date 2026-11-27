package pe.edu.sigestec.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.sigestec.entity.auth.RecuperacionClave;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.repository.auth.RecuperacionClaveRepository;
import pe.edu.sigestec.repository.auth.UsuarioRepository;

@Service
public class RecuperacionClaveService {
    private static final Logger LOG = LoggerFactory.getLogger(RecuperacionClaveService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RecuperacionClaveRepository recuperaciones;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final CorreoTransaccionalService correo;
    private final boolean enabled;
    private final boolean testMode;
    private final String testRecipient;
    private final String frontendUrl;
    private final int ttl;
    private final Map<String, LocalDateTime> ipUltimaPeticion = new ConcurrentHashMap<>();

    public RecuperacionClaveService(RecuperacionClaveRepository recuperaciones,
                UsuarioRepository usuarios, PasswordEncoder encoder, CorreoTransaccionalService correo,
                @Value("${app.recuperacion.enabled:false}") boolean enabled,
                @Value("${app.recuperacion.test-mode:false}") boolean testMode,
                @Value("${app.recuperacion.test-recipient:}") String testRecipient,
                @Value("${app.recuperacion.frontend-url:http://localhost:4200}") String frontendUrl,
                @Value("${app.recuperacion.ttl-minutos:15}") int ttl) {
        this.recuperaciones = recuperaciones;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.correo = correo;
        this.enabled = enabled;
        this.testMode = testMode;
        this.testRecipient = testRecipient;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.ttl = ttl;
        if (ttl < 1 || ttl > 60) throw new IllegalArgumentException("El token debe durar entre 1 y 60 minutos");
        if (testMode && !"http://localhost:4200".equals(this.frontendUrl)
                && !"http://127.0.0.1:4200".equals(this.frontendUrl)) {
            throw new IllegalStateException("El modo de correo compartido se permite solamente en localhost");
        }
    }

    private static LocalDateTime ahora() { return LocalDateTime.now(ZoneOffset.UTC); }

    @Transactional
    public void solicitar(String username, String clientIp) {
        if (!enabled || !correo.configurado()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura SMTP_USERNAME y SMTP_PASSWORD en IntelliJ");
        }

        LocalDateTime now = ahora();
        String ip = clientIp == null ? "unknown" : clientIp;
        LocalDateTime last = ipUltimaPeticion.get(ip);
        if (last != null && last.isAfter(now.minusSeconds(10))) return;
        if (ipUltimaPeticion.size() > 20000) ipUltimaPeticion.clear();
        ipUltimaPeticion.put(ip, now);

        Optional<Usuario> match = usuarios.findByNombreUsuario(username == null ? "" : username.trim());
        if (match.isEmpty() || !"A".equals(match.get().getEstado())) return;
        Usuario usuario = match.get();
        String destinatario = testMode ? testRecipient : usuario.getPersonaNatural().getCorreo();
        if (destinatario == null || destinatario.isBlank()) return;
        if (!destinatario.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) return;
        if (recuperaciones.countByUsuarioIdAndFechaCreacionAfter(usuario.getId(), now.minusHours(1)) >= 3) return;

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RecuperacionClave reset = new RecuperacionClave();
        reset.setUsuario(usuario);
        reset.setTokenHash(hash(token));
        reset.setFechaCreacion(now);
        reset.setFechaExpiracion(now.plusMinutes(ttl));
        recuperaciones.saveAndFlush(reset);
        String link = frontendUrl + "/restablecer-clave?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        try {
            correo.enviarRecuperacion(destinatario, link, ttl);
        } catch (RuntimeException e) {

            LOG.warn("No se pudo enviar el correo de recuperación. Comprueba la configuración SMTP, la contraseña de aplicación y la conexión. Tipo: {}",
                    e.getClass().getSimpleName());
            throw new ErrorEnvioRecuperacion();
        }
    }


    private static class ErrorEnvioRecuperacion extends RuntimeException {}

    @Transactional
    public void restablecer(String token, String nueva) {
        if (!enabled) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Recuperación deshabilitada");
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enlace inválido o caducado");
        }
        if (nueva == null || nueva.length() < 8 || nueva.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva clave debe tener entre 8 y 72 bytes");
        }
        RecuperacionClave reset = recuperaciones.bloquearPorHash(hash(token))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enlace inválido o caducado"));
        if (reset.getFechaUso() != null || !reset.getFechaExpiracion().isAfter(ahora()) ||
                !"A".equals(reset.getUsuario().getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enlace inválido o caducado");
        }
        Usuario usuario = reset.getUsuario();
        if (encoder.matches(nueva, usuario.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utiliza una contraseña diferente de la actual");
        }
        usuario.setClaveHash(encoder.encode(nueva));
        usuario.invalidarSesiones();
        usuarios.save(usuario);
        recuperaciones.invalidarPendientes(usuario.getId(), ahora());
    }

    private static String hash(String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
