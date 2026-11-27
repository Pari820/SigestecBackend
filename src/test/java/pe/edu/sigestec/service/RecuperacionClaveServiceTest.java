package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.sigestec.repository.auth.RecuperacionClaveRepository;
import pe.edu.sigestec.repository.auth.UsuarioRepository;

class RecuperacionClaveServiceTest {
    private final RecuperacionClaveRepository recuperaciones = mock(RecuperacionClaveRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final CorreoTransaccionalService correo = mock(CorreoTransaccionalService.class);

    private RecuperacionClaveService servicio(boolean enabled) {
        return new RecuperacionClaveService(recuperaciones, usuarios, encoder, correo,
                enabled, false, "", "http://localhost:4200", 15);
    }

    @Test
    void constructorRechazaTiempoDeVidaFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                new RecuperacionClaveService(recuperaciones, usuarios, encoder, correo,
                        true, false, "", "http://localhost:4200", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new RecuperacionClaveService(recuperaciones, usuarios, encoder, correo,
                        true, false, "", "http://localhost:4200", 61));
    }

    @Test
    void modoPruebaSoloPermiteFrontendLocalhost() {
        assertThrows(IllegalStateException.class, () ->
                new RecuperacionClaveService(recuperaciones, usuarios, encoder, correo,
                        true, true, "test@example.org", "https://sitio.example.org", 15));
    }

    @Test
    void solicitudRequiereRecuperacionYCorreoHabilitados() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio(false).solicitar("usuario", "127.0.0.1"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        verifyNoInteractions(usuarios, recuperaciones);
    }

    @Test
    void restablecimientoRechazaServicioDeshabilitado() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> servicio(false).restablecer("a".repeat(43), "ClaveSegura123"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
    }

    @Test
    void restablecimientoValidaFormatoDeTokenYLongitudDeClave() {
        RecuperacionClaveService habilitado = servicio(true);
        ResponseStatusException token = assertThrows(ResponseStatusException.class,
                () -> habilitado.restablecer("token-invalido", "ClaveSegura123"));
        assertEquals(HttpStatus.BAD_REQUEST, token.getStatusCode());

        ResponseStatusException claveCorta = assertThrows(ResponseStatusException.class,
                () -> habilitado.restablecer("A".repeat(43), "corta"));
        assertEquals(HttpStatus.BAD_REQUEST, claveCorta.getStatusCode());

        ResponseStatusException claveLarga = assertThrows(ResponseStatusException.class,
                () -> habilitado.restablecer("A".repeat(43), "x".repeat(73)));
        assertEquals(HttpStatus.BAD_REQUEST, claveLarga.getStatusCode());
        verifyNoInteractions(recuperaciones, usuarios, encoder);
    }
}
