package pe.edu.sigestec.config;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.Duration;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TokenService {
    private final SecretKey key;
    private final long expiracionEnSegundos;

    public TokenService(@Value("${app.jwt.secret:}") String configured,
                        @Value("${app.jwt.expiracion-minutos:120}") long expiracionMinutos) {
        if (expiracionMinutos < 1 || expiracionMinutos > 10080) {
            throw new IllegalArgumentException("app.jwt.expiracion-minutos debe estar entre 1 y 10080");
        }
        this.expiracionEnSegundos = Duration.ofMinutes(expiracionMinutos).toSeconds();
        byte[] bytes = (configured == null || configured.isBlank()) ? new byte[48] : Decoders.BASE64.decode(configured);
        if (configured == null || configured.isBlank()) new java.security.SecureRandom().nextBytes(bytes);
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
