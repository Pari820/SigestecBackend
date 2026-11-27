package pe.edu.sigestec.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.sigestec.auditoria.EventoAuditoria;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.almacen.MovimientoHerramientaEquipo;
import pe.edu.sigestec.entity.almacen.MovimientoMaterialAlmacen;
import pe.edu.sigestec.entity.asignaciones.AsignacionPersonal;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.AprobacionAts;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.cotizaciones.DetalleCotizacion;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.ejecuciones.MaterialUtilizado;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.DocumentoPlanificacion;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.storage.ArchivoStorageService;

@Service
@Transactional
public class GestionSeguraService {
    @Autowired
    private ApplicationEventPublisher auditoriaEventos;

    @PersistenceContext private EntityManager em;
    private final ArchivoStorageService storage;

    public GestionSeguraService(ArchivoStorageService storage) {
        this.storage = storage;
    }


    private static final Set<String> COMERCIAL = Set.of("GERENTE");
    private static final Set<String> PLANIFICACION = Set.of("SUPERVISOR");
    private static final Set<String> SEGURIDAD = Set.of("SSOMA");
    private static final Set<String> ALMACEN = Set.of("ALMACENERO");

    private void autorizar(String tipo) {
        Set<String> permitidos = switch (tipo) {
            case "solicitudes", "cotizaciones", "compras", "clientes", "tipos-servicio" -> COMERCIAL;
            case "ordenes-trabajo", "personal", "informes", "ejecuciones", "conformidades", "aprobaciones-ats" -> PLANIFICACION;
            case "ats" -> SEGURIDAD;
            case "materiales", "herramientas", "movimientos-materiales", "movimientos-herramientas" -> ALMACEN;
            default -> throw NegocioException.noExiste("Módulo no reconocido");
        };
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        boolean autorizado = a != null && a.isAuthenticated() && a.getAuthorities().stream()
                .anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN") || permitidos.stream()
                        .anyMatch(r -> x.getAuthority().equals("ROLE_" + r)));
        if (!autorizado) throw new NegocioException(HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para modificar este módulo");
    }

    private <T> T buscar(Class<T> clase, Long id, String nombre) {
        if (id == null || id <= 0) throw NegocioException.invalido("Identificador inválido");
        T valor = em.find(clase, id);
        if (valor == null) throw NegocioException.noExiste(nombre + " no encontrado");
        return valor;
    }

    private boolean existe(String entidad, String referencia, Long id) {

        return em.createQuery("SELECT COUNT(x) FROM " + entidad + " x WHERE x." + referencia + ".id = :id", Long.class)
                .setParameter("id", id).getSingleResult() > 0;
    }

    private static void validarCampos(Map<String, Object> datos, Set<String> permitidos) {
        if (datos == null || datos.isEmpty()) throw NegocioException.invalido("No hay campos para corregir");
        if (!permitidos.containsAll(datos.keySet()))
            throw NegocioException.invalido("No se permite alterar identificadores, relaciones, estados o cantidades de este registro");
    }

    private static String campo(Map<String, Object> d, String clave, String anterior, boolean obligatorio, int maximo) {
        if (!d.containsKey(clave)) return anterior;
        Object raw = d.get(clave);
        if (!(raw instanceof String)) throw NegocioException.invalido("Formato inválido para " + clave);
        String s = ((String) raw).trim();
        if (obligatorio && s.isBlank()) throw NegocioException.invalido("El campo " + clave + " es obligatorio");
        if (s.length() > maximo) throw NegocioException.invalido("El campo " + clave + " supera " + maximo + " caracteres");
        return s;
    }

    private static BigDecimal decimal(Map<String, Object> d, String clave, BigDecimal anterior) {
        if (!d.containsKey(clave)) return anterior;
        try {
            BigDecimal x = new BigDecimal(Objects.toString(d.get(clave), ""));
            if (x.signum() < 0 || x.scale() > 2) throw new NumberFormatException();
            return x;
        } catch (NumberFormatException e) {
            throw NegocioException.invalido("El campo " + clave + " debe ser un número no negativo con máximo 2 decimales");
        }
    }

    private static void exigir(boolean permitido, String mensaje) {
        if (!permitido) throw NegocioException.conflicto(mensaje);
    }

    public Map<String, String> editar(String tipo, Long id, Map<String, Object> d) {
        autorizar(tipo);
        switch (tipo) {
            case "solicitudes" -> {
                validarCampos(d, Set.of("descripcion", "observaciones", "lugarServicio"));
                SolicitudServicio s = buscar(SolicitudServicio.class, id, "Solicitud");
                exigir("PENDIENTE".equals(s.getEstado()) && !existe("Cotizacion", "solicitudServicio", id),
                        "No se puede editar: la solicitud ya forma parte de una cotización o cambió de estado");
                s.setDescripcion(campo(d, "descripcion", s.getDescripcion(), true, 1000));
                s.setObservaciones(campo(d, "observaciones", s.getObservaciones(), false, 1000));
                s.setLugarServicio(campo(d, "lugarServicio", s.getLugarServicio(), false, 300));
            }
            case "clientes" -> {
                validarCampos(d, Set.of("razonSocial", "telefono", "correo"));
                PersonaJuridica p = buscar(PersonaJuridica.class, id, "Cliente");
                exigir("A".equals(p.getEstado()), "Cliente inactivo: no se puede editar");
                p.setRazonSocial(campo(d, "razonSocial", p.getRazonSocial(), true, 180));
                p.setTelefono(campo(d, "telefono", p.getTelefono(), false, 20));
                p.setCorreo(campo(d, "correo", p.getCorreo(), false, 150));
            }
            case "tipos-servicio" -> {
                validarCampos(d, Set.of("nombre", "descripcion"));
                TipoServicio t = buscar(TipoServicio.class, id, "Tipo de servicio");
                exigir("A".equals(t.getEstado()), "Tipo de servicio inactivo");
                String nombre = campo(d, "nombre", t.getNombre(), true, 120);
                long duplicados = em.createQuery("SELECT COUNT(x) FROM TipoServicio x WHERE LOWER(x.nombre) = LOWER(:n) AND x.id <> :id", Long.class)
                        .setParameter("n", nombre).setParameter("id", id).getSingleResult();
                exigir(duplicados == 0, "Ya existe otro tipo de servicio con ese nombre");
                t.setNombre(nombre);
                t.setDescripcion(campo(d, "descripcion", t.getDescripcion(), false, 1000));
            }
            case "personal" -> {
                validarCampos(d, Set.of("nombre"));
                Empleado e = buscar(Empleado.class, id, "Empleado");
                exigir("A".equals(e.getEstado()), "Personal inactivo");
                String nombre = campo(d, "nombre", null, true, 220);
                int i = nombre.lastIndexOf(' ');
                exigir(i > 0, "Indica nombres y apellidos");
                e.getPersonaNatural().setNombres(nombre.substring(0, i));
                e.getPersonaNatural().setApellidos(nombre.substring(i + 1));
            }
            case "materiales" -> {
                validarCampos(d, Set.of("nombre", "unidadMedida", "stockMinimo"));
                Material m = buscar(Material.class, id, "Material");
                exigir("A".equals(m.getEstado()), "Material inactivo");
                if (d.containsKey("unidadMedida"))
                    exigir(!existe("MovimientoMaterialAlmacen", "material", id) && !existe("MaterialUtilizado", "material", id),
                            "No se puede cambiar la unidad: hay movimientos o consumos registrados");
                m.setNombre(campo(d, "nombre", m.getNombre(), true, 180));
                m.setUnidadMedida(campo(d, "unidadMedida", m.getUnidadMedida(), true, 30));
                m.setStockMinimo(decimal(d, "stockMinimo", m.getStockMinimo()));
            }
            case "herramientas" -> {
                validarCampos(d, Set.of("nombre", "descripcion"));
                HerramientaEquipo h = buscar(HerramientaEquipo.class, id, "Herramienta");
                exigir("A".equals(h.getEstado()), "Herramienta inactiva");
                h.setNombre(campo(d, "nombre", h.getNombre(), true, 180));
                h.setDescripcion(campo(d, "descripcion", h.getDescripcion(), false, 400));
            }
            case "compras" -> {
                validarCampos(d, Set.of("numero"));
                OrdenCompra o = buscar(OrdenCompra.class, id, "Orden de compra");
                exigir("REGISTRADA".equals(o.getEstado()) && !existe("OrdenTrabajo", "ordenCompra", id),
                        "No se puede corregir: la OC ya tiene una orden de trabajo o no está REGISTRADA");
                String numero = campo(d, "numero", o.getNumeroOrdenCompra(), true, 40);
                exigir(em.createQuery("SELECT COUNT(x) FROM OrdenCompra x WHERE LOWER(x.numeroOrdenCompra)=LOWER(:n) AND x.id <> :id",Long.class)
                        .setParameter("n", numero).setParameter("id", id).getSingleResult() == 0,
                        "El número de la orden de compra ya existe");
                o.setNumeroOrdenCompra(numero);
            }
            case "ats" -> {
                validarCampos(d, Set.of("condicionesArea", "observaciones"));
                AnalisisTrabajoSeguro a = buscar(AnalisisTrabajoSeguro.class, id, "ATS");
                exigir(Set.of("PENDIENTE", "ELABORADO").contains(a.getEstado())
                        && !existe("AprobacionAts", "analisisTrabajoSeguro", id)
                        && !existe("EjecucionServicio", "ordenTrabajo", a.getOrdenTrabajo().getId()),
                        "No se puede editar: el ATS ya tiene aprobación o ejecución");
                a.setCondicionesArea(campo(d, "condicionesArea", a.getCondicionesArea(), true, 1500));
                a.setObservaciones(campo(d, "observaciones", a.getObservaciones(), false, 1000));
            }
            case "ejecuciones" -> {
                validarCampos(d, Set.of("descripcion", "mediciones", "observaciones"));
                EjecucionServicio e = buscar(EjecucionServicio.class, id, "Ejecución");
                exigir(!existe("InformeTecnico", "ejecucionServicio", id),
                        "No se puede corregir: la ejecución ya tiene informe técnico");
                e.setDescripcion(campo(d, "descripcion", e.getDescripcion(), true, 1500));
                e.setMediciones(campo(d, "mediciones", e.getMediciones(), false, 1500));
                e.setObservaciones(campo(d, "observaciones", e.getObservaciones(), false, 1500));
            }
            case "informes" -> {
                validarCampos(d, Set.of("diagnostico", "trabajosRealizados", "conclusiones", "recomendaciones"));
                InformeTecnico i = buscar(InformeTecnico.class, id, "Informe técnico");
                exigir(!existe("ConformidadServicio", "informeTecnico", id),
                        "No se puede corregir: el informe ya tiene conformidad firmada");

                i.setDiagnostico(campo(d, "diagnostico", i.getDiagnostico(), true, 1500));
                i.setTrabajosRealizados(campo(d, "trabajosRealizados", i.getTrabajosRealizados(), true, 2000));
                i.setConclusiones(campo(d, "conclusiones", i.getConclusiones(), true, 1500));
                i.setRecomendaciones(campo(d, "recomendaciones", i.getRecomendaciones(), false, 1500));
            }
            case "ordenes-trabajo" -> throw NegocioException.conflicto("Para editar personal o programación usa las pantallas Asignaciones y Programaciones; no se altera una OT completa desde aquí");
            case "cotizaciones" -> throw NegocioException.conflicto("Para editar utiliza el formulario de Cotizaciones; conserva el cálculo de montos y el detalle del servicio");
            case "aprobaciones-ats", "conformidades", "movimientos-materiales", "movimientos-herramientas" ->
                    throw NegocioException.conflicto("Registro histórico: no se modifica directamente. Requiere una corrección o reversión documentada");
            default -> throw NegocioException.noExiste("Módulo no reconocido");
        }
        em.flush();
        auditoriaEventos.publishEvent(EventoAuditoria.de("REGISTRO_ACTUALIZADO", tipo, id,
                "Corrección de registro del módulo " + tipo));
        return Map.of("mensaje", "Cambios guardados en Oracle");
    }


    public Map<String, String> editarConDocumento(String tipo, Long id, Map<String, Object> datos,
                                                     MultipartFile pdf, boolean quitar) {
        editar(tipo, id, datos);
        if (quitar && pdf != null && !pdf.isEmpty())
            throw NegocioException.invalido("Elige quitar el documento o reemplazarlo, no ambos");
        boolean cambiar = quitar || (pdf != null && !pdf.isEmpty());
        if (!cambiar) return Map.of("mensaje", "Cambios guardados en Oracle");
        switch (tipo) {
            case "solicitudes" -> {
                SolicitudServicio e = buscar(SolicitudServicio.class, id, "Solicitud");
                e.setUrlArchivoAdjunto(quitar ? null : storage.guardar(pdf, true, true));
            }
            case "ats" -> {
                AnalisisTrabajoSeguro e = buscar(AnalisisTrabajoSeguro.class, id, "ATS");
                e.setUrlArchivo(quitar ? null : storage.guardar(pdf, true, true));
            }
            case "informes" -> {
                InformeTecnico e = buscar(InformeTecnico.class, id, "Informe");
                e.setUrlArchivo(quitar ? null : storage.guardar(pdf, true, true));
            }
            case "compras" -> {
                if (quitar) throw NegocioException.conflicto("La orden de compra debe conservar un PDF; adjunta uno nuevo para reemplazarlo");
                OrdenCompra e = buscar(OrdenCompra.class, id, "Orden de compra");
                e.setUrlArchivo(storage.guardar(pdf, true, true));
            }
            default -> throw NegocioException.conflicto("Este módulo no admite reemplazo de documento desde la corrección");
        }
        em.flush();
        auditoriaEventos.publishEvent(EventoAuditoria.de("DOCUMENTO_ACTUALIZADO", tipo, id,
                "Documento reemplazado o retirado"));
        return Map.of("mensaje", "Cambios y documento guardados en Oracle");
    }

    public Map<String, String> reactivar(String tipo, Long id) {
        autorizar(tipo);
        String mensaje;
        switch (tipo) {
            case "solicitudes" -> {
                SolicitudServicio s=buscar(SolicitudServicio.class,id,"Solicitud");
                exigir("CANCELADA".equals(s.getEstado()) && !existe("Cotizacion","solicitudServicio",id),
                        "La solicitud no se puede reactivar porque no está cancelada o tiene cotizaciones");
                s.setEstado("PENDIENTE"); mensaje="Solicitud reactivada para continuar el proceso";
            }
            case "compras" -> {
                OrdenCompra oc=buscar(OrdenCompra.class,id,"Orden de compra");
                exigir("ANULADA".equals(oc.getEstado()) && !existe("OrdenTrabajo","ordenCompra",id),
                        "No se puede reactivar la OC: tiene una orden de trabajo vinculada o no está anulada");
                oc.setEstado("REGISTRADA"); mensaje="Orden de compra reactivada";
            }
            case "ordenes-trabajo" -> {
                OrdenTrabajo ot=buscar(OrdenTrabajo.class,id,"Orden de trabajo");
                exigir("CANCELADA".equals(ot.getEstado()) && ot.getFechaProgramadaInicio()==null
                        && !existe("AnalisisTrabajoSeguro","ordenTrabajo",id)
                        && !existe("DocumentoPlanificacion","ordenTrabajo",id)
                        && !existe("MovimientoMaterialAlmacen","ordenTrabajo",id)
                        && !existe("MovimientoHerramientaEquipo","ordenTrabajo",id),
                        "La reactivación de una OT ya programada requiere revisar manualmente la disponibilidad del personal");
                ot.setEstado("ASIGNADA"); mensaje="OT reactivada: revisa su equipo antes de programar";
            }
            case "clientes" -> {
                PersonaJuridica c=buscar(PersonaJuridica.class,id,"Cliente");
                exigir("I".equals(c.getEstado()),"El cliente ya está activo");
                c.setEstado("A"); mensaje="Cliente reactivado";
            }
            case "tipos-servicio" -> {
                TipoServicio t=buscar(TipoServicio.class,id,"Tipo de servicio");
                exigir("I".equals(t.getEstado()),"El servicio ya está activo");
                t.setEstado("A"); mensaje="Tipo de servicio reactivado";
            }
            case "personal" -> {
                Empleado e=buscar(Empleado.class,id,"Empleado");
                exigir("I".equals(e.getEstado()),"El empleado ya está activo");
                e.setEstado("A"); mensaje="Empleado reactivado";
            }
            case "materiales" -> {
                Material m=buscar(Material.class,id,"Material");
                exigir("I".equals(m.getEstado()),"El material ya está activo");
                m.setEstado("A"); mensaje="Material reactivado";
            }
            case "herramientas" -> {
                HerramientaEquipo h=buscar(HerramientaEquipo.class,id,"Herramienta");
                exigir("I".equals(h.getEstado()),"La herramienta ya está activa");
                h.setEstado("A"); mensaje="Herramienta reactivada";
            }
            default -> throw NegocioException.conflicto("No existe reactivación automática para documentos históricos o decisiones aprobadas");
        }
        em.flush();
        auditoriaEventos.publishEvent(EventoAuditoria.de("REGISTRO_REACTIVADO", tipo, id, mensaje));
        return Map.of("mensaje",mensaje);
    }

    public Map<String, String> retirar(String tipo, Long id, String accion) {
        autorizar(tipo);
        if (!Set.of("eliminar", "cancelar").contains(accion)) throw NegocioException.invalido("Acción desconocida");
        String mensaje;
        switch (tipo) {
            case "solicitudes" -> {
                SolicitudServicio s = buscar(SolicitudServicio.class, id, "Solicitud");
                exigir("PENDIENTE".equals(s.getEstado()) && !existe("Cotizacion", "solicitudServicio", id),
                        "No se puede eliminar o cancelar: la solicitud ya tiene cotizaciones, órdenes o cambió de estado. Se conserva su historial");
                if (accion.equals("eliminar")) { em.remove(s); mensaje="Solicitud sin dependencias eliminada"; }
                else { s.setEstado("CANCELADA"); mensaje="Solicitud cancelada; se conserva el historial"; }
            }
            case "cotizaciones" -> {
                Cotizacion c = buscar(Cotizacion.class, id, "Cotización");
                exigir(!existe("OrdenCompra", "cotizacion", id),
                        "No se puede retirar: esta cotización ya tiene una orden de compra");
                exigir(Set.of("BORRADOR", "ENVIADA").contains(c.getEstado()),
                        "No se puede retirar una cotización respondida o finalizada");
                if (accion.equals("eliminar")) {
                    em.createQuery("DELETE FROM DetalleCotizacion x WHERE x.cotizacion.id = :id").setParameter("id", id).executeUpdate();
                    em.remove(c);
                    mensaje="Cotización sin orden de compra eliminada";
                } else { c.setEstado("RECHAZADA"); mensaje="Cotización rechazada sin eliminar el registro"; }
                SolicitudServicio s=c.getSolicitudServicio();
                if (!existe("OrdenCompra", "cotizacion", id)) {

                    boolean otras = em.createQuery("SELECT COUNT(x) FROM Cotizacion x WHERE x.solicitudServicio.id = :sid AND x.id <> :id AND x.estado <> 'RECHAZADA'",Long.class)
                            .setParameter("sid",s.getId()).setParameter("id",id).getSingleResult()>0;
                    if (!otras && "COTIZADA".equals(s.getEstado())) s.setEstado("PENDIENTE");
                }
            }
            case "compras" -> {
                OrdenCompra oc = buscar(OrdenCompra.class, id, "Orden de compra");
                if (accion.equals("eliminar")) throw NegocioException.conflicto("No se elimina una orden de compra emitida. Usa Cancelar si todavía no inició su flujo");
                exigir("REGISTRADA".equals(oc.getEstado()) && !existe("OrdenTrabajo", "ordenCompra", id),
                        "No se puede cancelar: la OC ya tiene una orden de trabajo o no está REGISTRADA");
                oc.setEstado("ANULADA"); mensaje="Orden de compra anulada; el documento se conserva";
            }
            case "ordenes-trabajo" -> {
                OrdenTrabajo ot=buscar(OrdenTrabajo.class,id,"Orden de trabajo");
                if (accion.equals("eliminar")) throw NegocioException.conflicto("No se eliminan órdenes de trabajo vinculadas a una OC. Usa Cancelar cuando sea seguro");
                exigir(!Set.of("CANCELADA", "EJECUTADA", "ATENDIDA").contains(ot.getEstado())
                        && !existe("AnalisisTrabajoSeguro", "ordenTrabajo",id)
                        && !existe("EjecucionServicio", "ordenTrabajo",id)
                        && !existe("InformeTecnico", "ordenTrabajo",id)
                        && !existe("ConformidadServicio", "ordenTrabajo",id)
                        && !existe("DocumentoPlanificacion", "ordenTrabajo",id)
                        && !existe("MovimientoMaterialAlmacen", "ordenTrabajo",id)
                        && !existe("MovimientoHerramientaEquipo", "ordenTrabajo",id),
                        "No se puede cancelar: la OT ya tiene ATS, documentos, movimientos de almacén o ejecución");
                ot.setEstado("CANCELADA");

                mensaje="Orden de trabajo cancelada; se conservaron OC y asignaciones";
            }
            case "clientes" -> {
                PersonaJuridica c=buscar(PersonaJuridica.class,id,"Cliente");
                exigir("A".equals(c.getEstado()), "El cliente ya está inactivo");
                long pendientes = em.createQuery("SELECT COUNT(x) FROM SolicitudServicio x WHERE x.empresaCliente.id = :id AND x.estado NOT IN ('ATENDIDA', 'CANCELADA')", Long.class)
                        .setParameter("id", id).getSingleResult();
                exigir(pendientes == 0, "No se puede inactivar el cliente: participa en solicitudes todavía en proceso");
                c.setEstado("I");mensaje="Cliente inactivado sin borrar solicitudes ni cotizaciones históricas";
            }
            case "tipos-servicio" -> {
                TipoServicio t=buscar(TipoServicio.class,id,"Tipo de servicio");
                exigir("A".equals(t.getEstado()), "El tipo de servicio ya está inactivo");
                long pendientes = em.createQuery("SELECT COUNT(x) FROM SolicitudServicio x WHERE x.tipoServicio.id = :id AND x.estado NOT IN ('ATENDIDA', 'CANCELADA')", Long.class)
                        .setParameter("id", id).getSingleResult();
                exigir(pendientes == 0, "No se puede inactivar este servicio: hay solicitudes en proceso que lo utilizan");
                t.setEstado("I");mensaje="Tipo de servicio inactivado; se conservan sus referencias";
            }
            case "personal" -> {
                Empleado e=buscar(Empleado.class,id,"Empleado");
                exigir("A".equals(e.getEstado()), "Empleado ya inactivo");
                boolean usuario = existe("Usuario", "personaNatural",e.getPersonaNatural().getId());
                exigir(!usuario, "El empleado tiene una cuenta de acceso. Primero debe gestionar el usuario y sus roles");
                boolean asignado=existe("AsignacionPersonal","empleado",id);
                exigir(!asignado,"El empleado tiene asignaciones; no se puede inactivar sin revisar las órdenes de trabajo vinculadas");
                e.setEstado("I");mensaje="Empleado inactivado sin borrar su historial";
            }
            case "materiales" -> {
                Material m=buscar(Material.class,id,"Material");
                exigir("A".equals(m.getEstado()), "Material ya inactivo");
                exigir(m.getStockActual().signum()==0,
                        "No se puede inactivar: el material tiene stock. Registra primero el movimiento correspondiente");
                m.setEstado("I");mensaje="Material inactivado; se conservaron sus movimientos";
            }
            case "herramientas" -> {
                HerramientaEquipo h=buscar(HerramientaEquipo.class,id,"Herramienta");
                exigir("A".equals(h.getEstado()), "Herramienta ya inactiva");
                exigir(h.getCantidadDisponible().compareTo(h.getCantidadTotal())==0,
                        "No se puede inactivar: existen préstamos o herramientas pendientes de devolución");
                h.setEstado("I");mensaje="Herramienta inactivada; su historial no fue eliminado";
            }
            case "ats" -> {
                AnalisisTrabajoSeguro a=buscar(AnalisisTrabajoSeguro.class,id,"ATS");
                throw NegocioException.conflicto("El ATS forma parte de la seguridad de una OT. Para anularlo debe registrarse una revisión documentada; no se elimina desde esta versión");
            }
            case "ejecuciones", "informes", "aprobaciones-ats", "conformidades", "movimientos-materiales", "movimientos-herramientas" ->
                    throw NegocioException.conflicto("Registro histórico: no puede eliminarse ni cancelarse sin una reversión documentada. Se conservan los datos y evidencias");
            default -> throw NegocioException.noExiste("Módulo no reconocido");
        }
        em.flush();
        auditoriaEventos.publishEvent(EventoAuditoria.de(
                "eliminar".equals(accion) ? "REGISTRO_ELIMINADO" : "REGISTRO_CANCELADO",
                tipo, id, mensaje));
        return Map.of("mensaje", mensaje);
    }
}
