package pe.edu.sigestec.service.impl.common;

import java.util.Map;
import org.springframework.stereotype.Service;
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
import pe.edu.sigestec.service.service.common.RespuestaService;

/** Adaptador de mapeo de respuestas; conserva exactamente las vistas originales. */
@Service
public class RespuestaServiceImpl implements RespuestaService {
    private final RespuestaComercialBuilder vistas;
    private final RespuestaOperativaBuilder vistasOperativas;

    public RespuestaServiceImpl(RespuestaComercialBuilder vistas, RespuestaOperativaBuilder vistasOperativas) {
        this.vistas = vistas;
        this.vistasOperativas = vistasOperativas;
    }

    @Override
    public Map<String, Object> cliente(PersonaJuridica entity) {
        return vistas.cliente(entity);
    }

    @Override
    public Map<String, Object> tipo(TipoServicio entity) {
        return vistas.tipo(entity);
    }

    @Override
    public Map<String, Object> empleado(Empleado entity) {
        return vistas.empleado(entity);
    }

    @Override
    public Map<String, Object> solicitud(SolicitudServicio entity) {
        return vistas.solicitud(entity);
    }

    @Override
    public Map<String, Object> cotizacion(Cotizacion entity) {
        return vistas.cotizacion(entity);
    }

    @Override
    public Map<String, Object> compra(OrdenCompra entity) {
        return vistas.compra(entity);
    }

    @Override
    public Map<String, Object> orden(OrdenTrabajo entity) {
        return vistas.orden(entity);
    }

    @Override
    public Map<String, Object> analisisTrabajoSeguro(AnalisisTrabajoSeguro entity) {
        return vistas.analisisTrabajoSeguro(entity);
    }

    @Override
    public Map<String, Object> aprobacion(AprobacionAts entity) {
        return vistasOperativas.aprobacion(entity);
    }

    @Override
    public Map<String, Object> material(Material entity) {
        return vistasOperativas.material(entity);
    }

    @Override
    public Map<String, Object> movimientoMaterial(MovimientoMaterialAlmacen entity) {
        return vistasOperativas.movimientoMaterial(entity);
    }

    @Override
    public Map<String, Object> herramienta(HerramientaEquipo entity) {
        return vistasOperativas.herramienta(entity);
    }

    @Override
    public Map<String, Object> movimientoHerramienta(MovimientoHerramientaEquipo entity) {
        return vistasOperativas.movimientoHerramienta(entity);
    }

    @Override
    public EjecucionServicioResponseDTO ejecucion(EjecucionServicio entity) {
        return vistasOperativas.ejecucion(entity);
    }

    @Override
    public InformeTecnicoResponseDTO informe(InformeTecnico entity) {
        return vistasOperativas.informe(entity);
    }

    @Override
    public ConformidadServicioResponseDTO conformidad(ConformidadServicio entity) {
        return vistasOperativas.conformidad(entity);
    }
}
