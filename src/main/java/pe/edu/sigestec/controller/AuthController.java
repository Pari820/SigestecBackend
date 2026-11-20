package pe.edu.sigestec.controller;

import pe.edu.sigestec.dto.Forms;
import pe.edu.sigestec.config.TokenService;
import pe.edu.sigestec.service.CuentaService;
import pe.edu.sigestec.service.RecuperacionClaveService;
import jakarta.servlet.http.HttpServletRequest;
import pe.edu.sigestec.entity.Usuario;
import pe.edu.sigestec.repository.*;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager manager;
    private final TokenService tokens;
    private final UsuarioRepository users;
    private final UsuarioRolRepository roles;
    private final CuentaService cuenta;
    private final RecuperacionClaveService recuperacion;

    public AuthController(AuthenticationManager manager, TokenService tokens, UsuarioRepository users,
                          UsuarioRolRepository roles, CuentaService cuenta, RecuperacionClaveService recuperacion) {
        this.manager = manager;
        this.tokens = tokens;
        this.users = users;
        this.roles = roles;
        this.cuenta = cuenta;
        this.recuperacion = recuperacion;
    }

    private Map<String, Object> resumen(Usuario u, boolean token) {
        String rol = roles.findByUsuarioId(u.getId()).stream().map(x -> x.getRol().getNombre()).findFirst().orElse("SIN_ROL");
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

    @PostMapping("/login")
    public Object login(@Valid @RequestBody Forms.Login x) {
        manager.authenticate(new UsernamePasswordAuthenticationToken(x.username(), x.password()));
        return resumen(users.findByNombreUsuario(x.username()).orElseThrow(), true);
    }

    /** Solo usuarios autenticados pueden cambiar su propia contraseña. */
    @PostMapping("/cambiar-clave")
    public Map<String, String> cambiarClave(Authentication auth, @Valid @RequestBody Forms.CambioClave datos) {
        cuenta.cambiarClave(auth.getName(), datos.claveActual(), datos.claveNueva());
        return Map.of("mensaje", "Contraseña actualizada. Inicia sesión nuevamente.");
    }

    @GetMapping("/me")
    public Object me(Authentication auth) {
        return resumen(users.findByNombreUsuario(auth.getName()).orElseThrow(), false);
    }

    /** Respuesta idéntica para cuentas registradas o inexistentes. */
    @PostMapping("/recuperar-clave")
    public Map<String, String> recuperarClave(@Valid @RequestBody Forms.SolicitarRecuperacion datos,
                                                HttpServletRequest request) {
        try {
            recuperacion.solicitar(datos.username(), request.getRemoteAddr());
        } catch (RuntimeException ex) {
            if (ex instanceof org.springframework.web.server.ResponseStatusException response &&
                    response.getStatusCode().value() == 503) throw response;
            // Nunca revelar si el usuario existe ni el estado de un envío particular.
        }
        return Map.of("mensaje", "Si la cuenta existe y tiene correo autorizado, recibirás instrucciones en unos minutos.");
    }

    @PostMapping("/restablecer-clave")
    public Map<String, String> restablecerClave(@Valid @RequestBody Forms.RestablecerClave datos) {
        recuperacion.restablecer(datos.token(), datos.claveNueva());
        return Map.of("mensaje", "La contraseña se actualizó. Inicia sesión con tu nueva contraseña.");
    }
}
