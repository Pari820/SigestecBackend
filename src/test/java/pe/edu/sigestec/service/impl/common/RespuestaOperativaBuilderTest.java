package pe.edu.sigestec.service.impl.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.sigestec.entity.almacen.*;
import pe.edu.sigestec.mapper.common.VistaCamposMapper;
import pe.edu.sigestec.repository.ejecuciones.EvidenciaEjecucionRepository;
import pe.edu.sigestec.repository.ejecuciones.MaterialUtilizadoRepository;
import pe.edu.sigestec.repository.informes.EvidenciaInformeTecnicoRepository;

class RespuestaOperativaBuilderTest {
    private RespuestaComercialBuilder vistas;
    private RespuestaOperativaBuilder builder;

    @BeforeEach
    void configurar() {
        vistas = mock(RespuestaComercialBuilder.class);
        builder = new RespuestaOperativaBuilder(
                vistas,
                mock(EvidenciaEjecucionRepository.class),
                mock(MaterialUtilizadoRepository.class),
                mock(EvidenciaInformeTecnicoRepository.class),
                new VistaCamposMapper());
    }

    @Test
    void transformaMaterialYCalculaIndicadorDeStockMinimo() {
        Material material = new Material();
        material.setId(8L);
        material.setCodigo(null);
        material.setNombre("Guantes");
        material.setUnidadMedida("PAR");
        material.setStockActual(new BigDecimal("2"));
        material.setStockMinimo(new BigDecimal("5"));
        material.setEstado(null);

        Map<String, Object> resultado = builder.material(material);

        assertEquals(8L, resultado.get("id"));
        assertEquals("", resultado.get("codigo"));
        assertEquals("Guantes", resultado.get("nombre"));
        assertEquals(Boolean.TRUE, resultado.get("bajoMinimo"));
        assertEquals("", resultado.get("estado"));
    }

    @Test
    void transformaHerramientaConCamposOpcionalesNulos() {
        HerramientaEquipo herramienta = new HerramientaEquipo();
        herramienta.setId(4L);
        herramienta.setCodigoHerramienta(null);
        herramienta.setNombre("Taladro");
        herramienta.setDescripcion(null);
        herramienta.setCantidadTotal(new BigDecimal("3"));
        herramienta.setCantidadDisponible(new BigDecimal("2"));
        herramienta.setEstado("A");

        Map<String, Object> resultado = builder.herramienta(herramienta);

        assertEquals(4L, resultado.get("id"));
        assertEquals("", resultado.get("codigo"));
        assertEquals("", resultado.get("descripcion"));
        assertEquals(new BigDecimal("3"), resultado.get("cantidadTotal"));
    }

    @Test
    void transformaMovimientoMaterialSinOrdenDeTrabajo() {
        Material material = new Material();
        material.setId(1L);
        material.setNombre("Cemento");
        material.setStockActual(BigDecimal.TEN);
        material.setStockMinimo(BigDecimal.ONE);

        MovimientoMaterialAlmacen movimiento = new MovimientoMaterialAlmacen();
        movimiento.setId(12L);
        movimiento.setMaterial(material);
        movimiento.setOrdenTrabajo(null);
        movimiento.setEmpleadoRegistra(null);
        movimiento.setTipoMovimiento("ENTRADA");
        movimiento.setCantidad(new BigDecimal("5"));
        movimiento.setMotivo(null);
        movimiento.setObservacion(null);

        Map<String, Object> resultado = builder.movimientoMaterial(movimiento);

        assertEquals(12L, resultado.get("id"));
        assertNull(resultado.get("ordenTrabajoId"));
        assertNull(resultado.get("ordenTrabajoNumero"));
        assertEquals("", resultado.get("motivo"));
        assertEquals("", resultado.get("observacion"));
        assertEquals("ENTRADA", resultado.get("tipoMovimiento"));
        verify(vistas).empleado(null);
    }

    @Test
    void transformaMovimientoHerramientaSinOrdenNiResponsable() {
        HerramientaEquipo herramienta = new HerramientaEquipo();
        herramienta.setId(3L);
        herramienta.setNombre("Llave");
        herramienta.setCantidadTotal(BigDecimal.ONE);
        herramienta.setCantidadDisponible(BigDecimal.ONE);

        MovimientoHerramientaEquipo movimiento = new MovimientoHerramientaEquipo();
        movimiento.setId(15L);
        movimiento.setHerramientaEquipo(herramienta);
        movimiento.setOrdenTrabajo(null);
        movimiento.setEmpleadoResponsable(null);
        movimiento.setEmpleadoRegistra(null);
        movimiento.setTipoMovimiento("DEVOLUCION");
        movimiento.setCantidad(BigDecimal.ONE);
        movimiento.setEstadoDevolucion(null);
        movimiento.setObservacion(null);

        Map<String, Object> resultado = builder.movimientoHerramienta(movimiento);

        assertEquals(15L, resultado.get("id"));
        assertNull(resultado.get("ordenTrabajoId"));
        assertNull(resultado.get("empleadoResponsable"));
        assertEquals("", resultado.get("estadoDevolucion"));
        assertEquals("", resultado.get("observacion"));
        verify(vistas).empleado(null);
    }
}
