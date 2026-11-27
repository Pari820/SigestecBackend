package pe.edu.sigestec.service.service.common;

import java.util.Map;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioResponseDTO;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioResponseDTO;
import pe.edu.sigestec.dto.informes.InformeTecnicoResponseDTO;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.AprobacionAts;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.service.*;

/** Contrato para convertir entidades a las respuestas HTTP de SIGESTEC. */
public interface RespuestaService {
    Map<String, Object> cliente(PersonaJuridica entity);
    Map<String, Object> tipo(TipoServicio entity);
    Map<String, Object> empleado(Empleado entity);
    Map<String, Object> solicitud(SolicitudServicio entity);
    Map<String, Object> cotizacion(Cotizacion entity);
    Map<String, Object> compra(OrdenCompra entity);
    Map<String, Object> orden(OrdenTrabajo entity);
    Map<String, Object> analisisTrabajoSeguro(AnalisisTrabajoSeguro entity);
    Map<String, Object> aprobacion(AprobacionAts entity);
    Map<String, Object> material(Material entity);
    Map<String, Object> movimientoMaterial(MovimientoMaterialAlmacen entity);
    Map<String, Object> herramienta(HerramientaEquipo entity);
    Map<String, Object> movimientoHerramienta(MovimientoHerramientaEquipo entity);
    EjecucionServicioResponseDTO ejecucion(EjecucionServicio entity);
    InformeTecnicoResponseDTO informe(InformeTecnico entity);
    ConformidadServicioResponseDTO conformidad(ConformidadServicio entity);
}
