package pe.edu.sigestec.mapper.common;

import java.util.Map;
import org.springframework.stereotype.Component;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.personal.Empleado;

/** Convierte entidades en campos simples de las respuestas sin consultar repositorios. */
@Component
public class VistaCamposMapper {

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    public Map<String, Object> tipo(TipoServicio tipo) {
        return Map.of("id", tipo.getId(), "numeroRegistro", tipo.getNumeroRegistro(), "nombre", tipo.getNombre(),
                "descripcion", texto(tipo.getDescripcion()), "estado", tipo.getEstado());
    }

    public Map<String, Object> empleado(Empleado empleado) {
        String nombre = empleado.getPersonaNatural().getNombres() + " "
                + empleado.getPersonaNatural().getApellidos();
        return Map.of("id", empleado.getId(), "numeroRegistro", empleado.getNumeroRegistro(), "nombre", nombre.trim(),
                "perfil", empleado.getCargo().getNombre(), "activo", "A".equals(empleado.getEstado()));
    }

    public Map<String, Object> material(Material material) {
        return Map.of("id", material.getId(), "codigo", texto(material.getCodigo()),
                "nombre", texto(material.getNombre()), "unidadMedida", texto(material.getUnidadMedida()),
                "stockActual", material.getStockActual(), "stockMinimo", material.getStockMinimo(),
                "estado", texto(material.getEstado()),
                "bajoMinimo", material.getStockActual().compareTo(material.getStockMinimo()) <= 0);
    }

    public Map<String, Object> herramienta(HerramientaEquipo herramienta) {
        return Map.of("id", herramienta.getId(), "codigo", texto(herramienta.getCodigoHerramienta()),
                "nombre", texto(herramienta.getNombre()), "descripcion", texto(herramienta.getDescripcion()),
                "cantidadTotal", herramienta.getCantidadTotal(),
                "cantidadDisponible", herramienta.getCantidadDisponible(),
                "estado", texto(herramienta.getEstado()));
    }
}
