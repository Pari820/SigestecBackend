package pe.edu.sigestec.service.impl;

import pe.edu.sigestec.entity.Usuario;
import pe.edu.sigestec.repository.UsuarioRepository;
import pe.edu.sigestec.repository.RecuperacionClaveRepository;
import pe.edu.sigestec.service.CuentaService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
public class CuentaServiceImpl implements CuentaService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final RecuperacionClaveRepository recuperaciones;

    public CuentaServiceImpl(UsuarioRepository usuarios, PasswordEncoder encoder, RecuperacionClaveRepository recuperaciones) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.recuperaciones = recuperaciones;
    }

    @Override
    @Transactional
    public void cambiarClave(String username, String claveActual, String claveNueva) {
        Usuario usuario = usuarios.findByNombreUsuario(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cuenta no encontrada"));
        if (!"A".equals(usuario.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta inactiva");
        }
        if (!encoder.matches(claveActual, usuario.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        if (claveNueva == null || claveNueva.length() < 8 ||
                claveNueva.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe tener entre 8 y 72 bytes");
        }
        if (encoder.matches(claveNueva, usuario.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe ser diferente de la actual");
        }
        usuario.setClaveHash(encoder.encode(claveNueva));
        usuario.invalidarSesiones();
        recuperaciones.invalidarPendientes(usuario.getId(), java.time.LocalDateTime.now(java.time.Clock.systemUTC()));
        usuarios.save(usuario);
    }
}
