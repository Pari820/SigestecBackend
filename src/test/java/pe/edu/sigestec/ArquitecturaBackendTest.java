package pe.edu.sigestec;

import org.junit.jupiter.api.Test;
import pe.edu.sigestec.dto.common.ErrorResponseDTO;
import pe.edu.sigestec.dto.cotizaciones.EstadoRequestDTO;
import pe.edu.sigestec.enums.DecisionCotizacion;
import pe.edu.sigestec.enums.TipoMovimientoMaterial;
import pe.edu.sigestec.mapper.common.VistaCamposMapper;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas que no requieren conectarse a Oracle. */
class ArquitecturaBackendTest {

    @Test
    void controladoresNoInyectanRepositorios() throws ClassNotFoundException {
        List<String> controladores = List.of(
                "pe.edu.sigestec.controller.almacen.AlmacenController",
                "pe.edu.sigestec.controller.archivos.ArchivoController",
                "pe.edu.sigestec.controller.asignaciones.AsignacionPersonalController",
                "pe.edu.sigestec.controller.ats.AnalisisTrabajoSeguroController",
                "pe.edu.sigestec.controller.ats.AprobacionAtsController",
                "pe.edu.sigestec.controller.auth.AuthController",
                "pe.edu.sigestec.controller.catalogos.CatalogosController",
                "pe.edu.sigestec.controller.compras.OrdenCompraController",
                "pe.edu.sigestec.controller.conformidades.ConformidadServicioController",
                "pe.edu.sigestec.controller.cotizaciones.CotizacionController",
                "pe.edu.sigestec.controller.ejecuciones.EjecucionServicioController",
                "pe.edu.sigestec.controller.gestion.GestionSeguraController",
                "pe.edu.sigestec.controller.gestion.HerramientaOperacionController",
                "pe.edu.sigestec.controller.informes.InformeTecnicoController",
                "pe.edu.sigestec.controller.integraciones.ConsultaDniController",
                "pe.edu.sigestec.controller.integraciones.ConsultaSunatController",
                "pe.edu.sigestec.controller.orden_trabajo.OrdenTrabajoController",
                "pe.edu.sigestec.controller.solicitudes.SolicitudServicioController"
        );

        for (String nombre : controladores) {
            Class<?> controller = Class.forName(nombre);
            for (Field campo : controller.getDeclaredFields()) {
                assertFalse(campo.getType().getSimpleName().endsWith("Repository"),
                        () -> nombre + " depende de Repository: " + campo.getName());
            }
        }
        assertEquals(18, controladores.size());
    }

    @Test
    void mapperNoRealizaConsultas() {
        for (Field campo : VistaCamposMapper.class.getDeclaredFields()) {
            assertFalse(campo.getType().getSimpleName().endsWith("Repository"));
        }
    }

    @Test
    void dtoDeEstadoConservaElContrato() {
        EstadoRequestDTO dto = new EstadoRequestDTO("ACEPTADA");
        assertEquals("ACEPTADA", dto.estado());
        assertTrue(DecisionCotizacion.permite(dto.estado()));
        assertFalse(DecisionCotizacion.permite("DESCONOCIDA"));
        assertEquals("DEVOLUCION", TipoMovimientoMaterial.DEVOLUCION.name());
    }

    @Test
    void errorSigueTeniendoElCampoMensaje() throws Exception {
        ErrorResponseDTO error = new ErrorResponseDTO(
                "2026-11-23T12:00:00Z", 400, "Bad Request", "Datos incorrectos",
                "/api/v1/solicitudes", null, null);
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(error);
        assertTrue(json.contains("\"mensaje\":\"Datos incorrectos\""));
        assertTrue(json.contains("\"status\":400"));
    }
}
