package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.auth.UsuarioRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;

class UsuarioActualServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final EmpleadoRepository empleados = mock(EmpleadoRepository.class);
    private final UsuarioActualService service = new UsuarioActualService(usuarios, empleados);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void exigeSesionAutenticada() {
        SecurityContextHolder.clearContext();
        assertThrows(NegocioException.class, service::empleado);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario", null));
        assertThrows(NegocioException.class, service::empleado);
        verifyNoInteractions(usuarios, empleados);
    }

    @Test
    void rechazaUsuarioNoRegistrado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("desconocido", null, java.util.List.of()));
        when(usuarios.findByNombreUsuario("desconocido")).thenReturn(Optional.empty());

        assertThrows(NegocioException.class, service::empleado);
        verifyNoInteractions(empleados);
    }

    @Test
    void rechazaUsuarioSinEmpleadoAsociado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario", null, java.util.List.of()));
        Usuario usuario = mock(Usuario.class);
        var persona = mock(pe.edu.sigestec.entity.catalogos.PersonaNatural.class);
        when(usuarios.findByNombreUsuario("usuario")).thenReturn(Optional.of(usuario));
        when(usuario.getPersonaNatural()).thenReturn(persona);
        when(persona.getId()).thenReturn(7L);
        when(empleados.findByPersonaNaturalId(7L)).thenReturn(Optional.empty());

        assertThrows(NegocioException.class, service::empleado);
    }
}
