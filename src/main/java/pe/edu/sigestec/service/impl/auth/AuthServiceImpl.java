package pe.edu.sigestec.service.impl.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import pe.edu.sigestec.service.service.auditoria.AuditoriaService;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.repository.auth.UsuarioRepository;
import pe.edu.sigestec.repository.auth.UsuarioRolRepository;
import pe.edu.sigestec.security.TokenService;
import pe.edu.sigestec.service.service.auth.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private AuditoriaService auditoria;
    private final AuthenticationManager manager;
    private final TokenService tokens;
    private final UsuarioRepository users;
    private final UsuarioRolRepository roles;

    public AuthServiceImpl(AuthenticationManager manager, TokenService tokens,
                           UsuarioRepository users, UsuarioRolRepository roles) {
        this.manager = manager;
        this.tokens = tokens;
        this.users = users;
        this.roles = roles;
    }

    private Map<String, Object> resumen(Usuario u, boolean token) {
        String rol = roles.findByUsuarioId(u.getId()).stream()
            .map(x -> x.getRol().getNombre())
            .findFirst().orElse("SIN_ROL");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rol", rol);
        m.put("nombre", u.getPersonaNatural().getNombres() + " " + u.getPersonaNatural().getApellidos());
        m.put("username", u.getNombreUsuario());
        if (token) {
            m.put("token", tokens.create(u.getNombreUsuario(), u.getTokenVersion()));
            m.put("expiraEnSegundos", tokens.expiracionEnSegundos());
        }
        return m;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO datos) {
        try {
            manager.authenticate(new UsernamePasswordAuthenticationToken(datos.username(), datos.password()));
        } catch (AuthenticationException ex) {
            auditarLogin("LOGIN_FALLIDO", datos.username());
            throw ex;
        }
        Map<String, Object> r = resumen(users.findByNombreUsuario(datos.username()).orElseThrow(), true);
        auditarLogin("LOGIN_EXITOSO", datos.username());
        return new LoginResponseDTO((String) r.get("rol"), (String) r.get("nombre"),
            (String) r.get("username"), (String) r.get("token"), (long) r.get("expiraEnSegundos"));
    }

    private void auditarLogin(String accion, String username) {
        try { auditoria.registrar(EventoAuditoria.login(accion, username)); }
        catch (RuntimeException ex) {
            log.error("No se pudo guardar auditoría de autenticación {}", accion, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO miCuenta(String username) {
        Map<String, Object> r = resumen(users.findByNombreUsuario(username).orElseThrow(), false);
        return new CuentaResponseDTO((String) r.get("rol"), (String) r.get("nombre"), (String) r.get("username"));
    }
}
