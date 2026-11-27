package pe.edu.sigestec.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.service.RecuperacionClaveService;
import pe.edu.sigestec.service.service.auth.AuthService;
import pe.edu.sigestec.service.service.auth.CuentaService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final CuentaService cuenta;
    private final RecuperacionClaveService recuperacion;

    public AuthController(AuthService authService, CuentaService cuenta,
                          RecuperacionClaveService recuperacion) {
        this.authService = authService;
        this.cuenta = cuenta;
        this.recuperacion = recuperacion;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO datos) {
        return authService.login(datos);
    }

    @PostMapping("/cambiar-clave")
    public Map<String, String> cambiarClave(Authentication auth,
                                           @Valid @RequestBody CambioClaveRequestDTO datos) {
        cuenta.cambiarClave(auth.getName(), datos.claveActual(), datos.claveNueva());
        return Map.of("mensaje", "Contraseña actualizada. Inicia sesión nuevamente.");
    }

    @GetMapping("/me")
    public CuentaResponseDTO me(Authentication auth) {
        return authService.miCuenta(auth.getName());
    }

    @PostMapping("/recuperar-clave")
    public Map<String, String> recuperarClave(@Valid @RequestBody SolicitarRecuperacionRequestDTO datos,
                                              HttpServletRequest request) {
        try {
            recuperacion.solicitar(datos.username(), request.getRemoteAddr());
        } catch (RuntimeException ex) {
            if (ex instanceof org.springframework.web.server.ResponseStatusException response &&
                    response.getStatusCode().value() == 503) throw response;
        }
        return Map.of("mensaje", "Si la cuenta existe y tiene correo autorizado, recibirás instrucciones en unos minutos.");
    }

    @PostMapping("/restablecer-clave")
    public Map<String, String> restablecerClave(@Valid @RequestBody RestablecerClaveRequestDTO datos) {
        recuperacion.restablecer(datos.token(), datos.claveNueva());
        return Map.of("mensaje", "La contraseña se actualizó. Inicia sesión con tu nueva contraseña.");
    }
}
