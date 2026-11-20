package pe.edu.sigestec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.Forms;
import pe.edu.sigestec.entity.*;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.*;
import pe.edu.sigestec.service.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas con H2 temporal; nunca borra datos de Oracle. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sigestec_gestion_segura;MODE=Oracle;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "app.demo.enabled=true",
        "app.storage.directory=./archivos-sigestec-test"
})
@Transactional
class GestionSeguraTest {
    @Autowired GestionSeguraService gestion;
    @Autowired SolicitudServicioService solicitudes;
    @Autowired CotizacionService cotizaciones;
    @Autowired SolicitudServicioRepository solicitudesRepo;
    @Autowired PersonaJuridicaRepository clientes;
    @Autowired TipoServicioRepository tipos;

    private void rol(String usuario, String rol) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }
    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    private SolicitudServicio solicitud() {
        rol("gerente", "GERENTE");
        return solicitudes.registrar(new Forms.SolicitudForm(
                clientes.findAll().get(0).getId(), tipos.findAll().get(0).getId(),
                "Prueba operativa", "Observación inicial", null, null));
    }
    private Forms.CotizacionForm cotizacion(Long solicitudId) {
        return new Forms.CotizacionForm(solicitudId, "Prueba", "Test",
                List.of(new Forms.DetalleCotizacionForm("Servicio", BigDecimal.ONE, new BigDecimal("200.00"), "SERVICIO")));
    }

    @Test void permiteCorregirYEliminarSolicitudIndependiente() {
        SolicitudServicio s=solicitud();
        gestion.editar("solicitudes",s.getId(), Map.of("descripcion", "Texto corregido"));
        assertEquals("Texto corregido", solicitudesRepo.findById(s.getId()).orElseThrow().getDescripcion());
        gestion.retirar("solicitudes",s.getId(),"eliminar");
        assertFalse(solicitudesRepo.findById(s.getId()).isPresent());
    }

    @Test void impideEliminarSolicitudConCotizacionYProtegeLaCotizacionAceptada() {
        SolicitudServicio s=solicitud();
        Cotizacion c=cotizaciones.registrar(cotizacion(s.getId()));
        assertThrows(NegocioException.class,()->gestion.retirar("solicitudes",s.getId(),"eliminar"));
        assertThrows(NegocioException.class,()->gestion.editar("solicitudes",s.getId(),Map.of("descripcion","No permitido")));
        cotizaciones.cambiarEstado(c.getId(),"ACEPTADA");
        assertThrows(NegocioException.class,()->gestion.retirar("cotizaciones",c.getId(),"eliminar"));
        assertTrue(solicitudesRepo.findById(s.getId()).isPresent());
    }

    @Test void puedeRechazarUnaCotizacionNoRespondidaSinPerderLaSolicitud() {
        SolicitudServicio s=solicitud();
        Cotizacion c=cotizaciones.registrar(cotizacion(s.getId()));
        gestion.retirar("cotizaciones",c.getId(),"cancelar");
        assertEquals("RECHAZADA",c.getEstado());
        assertEquals("PENDIENTE",s.getEstado());
    }

    @Test void rolSsomaNoPuedeModificarSolicitudesComerciales() {
        SolicitudServicio s=solicitud();
        rol("ssoma", "SSOMA");
        NegocioException e=assertThrows(NegocioException.class,
                ()->gestion.editar("solicitudes",s.getId(),Map.of("descripcion","Prohibido")));
        assertEquals(403,e.getStatus().value());
    }

    @Test void documentosDefinitivosNoSeEliminan() {
        rol("supervisor", "SUPERVISOR");
        assertThrows(NegocioException.class,()->gestion.retirar("conformidades",1L,"eliminar"));
        assertThrows(NegocioException.class,()->gestion.retirar("aprobaciones-ats",1L,"cancelar"));
    }
}
