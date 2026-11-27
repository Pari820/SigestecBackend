package pe.edu.sigestec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

class CorrelativoDocumentoServiceTest {
    private final EntityManager em = mock(EntityManager.class);
    private final CorrelativoDocumentoService service = new CorrelativoDocumentoService(em);

    @Test
    void validaTipoPrefijoYLongitudDelCorrelativo() {
        assertThrows(IllegalArgumentException.class, () -> service.siguiente(null, "COT", 2026, 3));
        assertThrows(IllegalArgumentException.class, () -> service.siguiente("COT", " ", 2026, 3));
        assertThrows(IllegalArgumentException.class, () -> service.siguiente("COT", "COT", 2026, 0));
        assertThrows(IllegalArgumentException.class, () -> service.siguienteSinAnio("COT", "COT", 13));
        assertThrows(IllegalArgumentException.class, () -> service.siguienteConAnioDinamico("COT", "COT", 0, 3));
        assertThrows(IllegalArgumentException.class, () -> service.siguienteNumeroSinAnio(" "));
        verifyNoInteractions(em);
    }

    @Test
    void informaCuandoNoExisteContadorAnual() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(IllegalStateException.class,
                () -> service.siguiente("COTIZACION", "COT", 2026, 3));
    }

    @Test
    void informaCuandoNoExisteContadorBaseParaAnioDinamico() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());

        assertThrows(IllegalStateException.class,
                () -> service.siguienteConAnioDinamico("ACTA", "ACT", 2026, 3));
    }
}
