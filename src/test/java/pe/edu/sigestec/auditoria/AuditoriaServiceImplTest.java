package pe.edu.sigestec.auditoria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.sigestec.entity.auditoria.AuditoriaEvento;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.auditoria.AuditoriaEventoRepository;
import pe.edu.sigestec.service.impl.auditoria.AuditoriaServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceImplTest {
    @Mock AuditoriaEventoRepository repository;

    @Test
    void registrarNoGuardaClavesNiTokensEnElEvento() {
        AuditoriaServiceImpl service = new AuditoriaServiceImpl(repository);
        service.registrar(new EventoAuditoria("supervisor", "ATS_REVISADO", "AprobacionAts", 7L,
                "Resultado: APROBADO", "abcd1234"));
        ArgumentCaptor<AuditoriaEvento> captor = ArgumentCaptor.forClass(AuditoriaEvento.class);
        verify(repository).save(captor.capture());
        assertEquals("supervisor", captor.getValue().getUsuario());
        assertEquals("ATS_REVISADO", captor.getValue().getAccion());
        assertEquals(7L, captor.getValue().getEntidadId());
        assertNotNull(captor.getValue().getFecha());
    }

    @Test
    void rechazaPaginacionInvalida() {
        AuditoriaServiceImpl service = new AuditoriaServiceImpl(repository);
        assertThrows(NegocioException.class, () -> service.buscar(null, null, null, null, -1, 20));
        assertThrows(NegocioException.class, () -> service.buscar(null, null, null, null, 0, 101));
        verifyNoInteractions(repository);
    }

    @Test
    void rechazaFechasInvertidas() {
        AuditoriaServiceImpl service = new AuditoriaServiceImpl(repository);
        assertThrows(NegocioException.class,
                () -> service.buscar(null, null, LocalDate.of(2026, 11, 26), LocalDate.of(2026, 11, 20), 0, 20));
        verifyNoInteractions(repository);
    }
}
