package pe.edu.sigestec.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final SecretKey key;
    private final long expiracionEnSegundos;

    public TokenService(@Value("${app.jwt.secret:}") String configured,
                        @Value("${app.jwt.expiracion-minutos:120}") long expiracionMinutos) {
        if (expiracionMinutos < 1 || expiracionMinutos > 10080) {
            throw new IllegalArgumentException("app.jwt.expiracion-minutos debe estar entre 1 y 10080");
        }
        this.expiracionEnSegundos = Duration.ofMinutes(expiracionMinutos).toSeconds();
        boolean secretoTemporal = configured == null || configured.isBlank();
        byte[] bytes = secretoTemporal ? new byte[48] : Decoders.BASE64.decode(configured);
        if (secretoTemporal) {
            new java.security.SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET_BASE64 no está configurado. Se usará una clave temporal; "
                    + "todas las sesiones dejarán de ser válidas cuando se reinicie el backend. "
                    + "Configura una clave Base64 estable en IntelliJ.");
        }
        if (bytes.length < 48)
            throw new IllegalArgumentException("JWT_SECRET_BASE64 debe contener al menos 48 bytes aleatorios");
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String create(String username, long version) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).claim("sv", version).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiracionEnSegundos)))
                .signWith(key, Jwts.SIG.HS384).compact();
    }

    public long expiracionEnSegundos() {
        return expiracionEnSegundos;
    }

    public io.jsonwebtoken.Claims validar(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

}
