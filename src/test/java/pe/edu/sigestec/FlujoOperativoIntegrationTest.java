package pe.edu.sigestec;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.dto.almacen.HerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MaterialRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoHerramientaRequestDTO;
import pe.edu.sigestec.dto.almacen.MovimientoMaterialRequestDTO;
import pe.edu.sigestec.dto.asignaciones.AsignacionPersonalRequestDTO;
import pe.edu.sigestec.dto.ats.AnalisisTrabajoSeguroRequestDTO;
import pe.edu.sigestec.dto.ats.AprobacionAtsRequestDTO;
import pe.edu.sigestec.dto.ats.AtsEtapaDTO;
import pe.edu.sigestec.dto.conformidades.ConformidadServicioRequestDTO;
import pe.edu.sigestec.dto.cotizaciones.CotizacionRequestDTO;
import pe.edu.sigestec.dto.cotizaciones.DetalleCotizacionRequestDTO;
import pe.edu.sigestec.dto.ejecuciones.EjecucionServicioRequestDTO;
import pe.edu.sigestec.dto.ejecuciones.MaterialRealDTO;
import pe.edu.sigestec.dto.informes.InformeTecnicoRequestDTO;
import pe.edu.sigestec.dto.solicitudes.SolicitudServicioRequestDTO;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;
import pe.edu.sigestec.entity.almacen.Material;
import pe.edu.sigestec.entity.ats.AnalisisTrabajoSeguro;
import pe.edu.sigestec.entity.ats.Riesgo;
import pe.edu.sigestec.entity.conformidades.ConformidadServicio;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.ats.AnalisisTrabajoSeguroRepository;
import pe.edu.sigestec.repository.catalogos.PersonaJuridicaRepository;
import pe.edu.sigestec.repository.catalogos.TipoServicioRepository;
import pe.edu.sigestec.repository.ejecuciones.MaterialUtilizadoRepository;
import pe.edu.sigestec.repository.orden_trabajo.OrdenTrabajoRepository;
import pe.edu.sigestec.repository.personal.EmpleadoRepository;
import pe.edu.sigestec.service.*;
import pe.edu.sigestec.service.service.almacen.MovimientoHerramientaEquipoService;
import pe.edu.sigestec.service.service.almacen.MovimientoMaterialAlmacenService;
import pe.edu.sigestec.service.service.asignaciones.AsignacionPersonalService;
import pe.edu.sigestec.service.service.ats.AnalisisTrabajoSeguroService;
import pe.edu.sigestec.service.service.ats.AprobacionAtsService;
import pe.edu.sigestec.service.service.compras.OrdenCompraService;
import pe.edu.sigestec.service.service.conformidades.ConformidadServicioService;
import pe.edu.sigestec.service.service.cotizaciones.CotizacionService;
import pe.edu.sigestec.service.service.ejecuciones.EjecucionServicioService;
import pe.edu.sigestec.service.service.informes.InformeTecnicoService;
import pe.edu.sigestec.service.service.orden_trabajo.OrdenTrabajoService;
import pe.edu.sigestec.service.service.solicitudes.SolicitudServicioService;

@Disabled("Requiere una base Oracle exclusiva para pruebas de integración")
@SpringBootTest
@Transactional
class FlujoOperativoIntegrationTest {
    @Autowired SolicitudServicioService solicitudes;
    @Autowired CotizacionService cotizaciones;
    @Autowired OrdenCompraService compras;
    @Autowired AsignacionPersonalService asignaciones;
    @Autowired OrdenTrabajoService programacion;
    @Autowired AnalisisTrabajoSeguroService analisis;
    @Autowired AprobacionAtsService aprobaciones;
    @Autowired EjecucionServicioService ejecuciones;
    @Autowired InformeTecnicoService informes;
    @Autowired ConformidadServicioService conformidades;
    @Autowired MovimientoMaterialAlmacenService materialService;
    @Autowired MovimientoHerramientaEquipoService herramientaService;
    @Autowired PersonaJuridicaRepository empresas;
    @Autowired TipoServicioRepository tipos;
    @Autowired EmpleadoRepository empleados;
    @Autowired AnalisisTrabajoSeguroRepository atsRepo;
    @Autowired MaterialUtilizadoRepository declarados;
    @Autowired OrdenTrabajoRepository ordenes;

    private void usuario(String nombre){
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(nombre,null,Collections.emptyList()));
    }
    @AfterEach void limpiar(){SecurityContextHolder.clearContext();}
    private MockMultipartFile pdf(String nombre){
        return new MockMultipartFile("pdf",nombre,"application/pdf","%PDF-1.4\n%%EOF".getBytes());
    }
    private MockMultipartFile foto(String nombre){
        return new MockMultipartFile("antes",nombre,"image/png",new byte[]{(byte)137,80,78,71,13,10,26,10,0,0,0,0});
    }
    private OrdenTrabajo prepararOT(){
        usuario("gerente");
        SolicitudServicio solicitud=solicitudes.registrar(new SolicitudServicioRequestDTO(
            empresas.findAll().get(0).getId(), tipos.findAll().get(0).getId(),
            "Inspección eléctrica", "Observación",null,null));
        Cotizacion cot=cotizaciones.registrar(new CotizacionRequestDTO(solicitud.getId(),"Inspección",
            "Cotización",List.of(new DetalleCotizacionRequestDTO("Revisión",BigDecimal.ONE,new BigDecimal("200.00"),"SERVICIO"))));
        cotizaciones.cambiarEstado(cot.getId(),"ACEPTADA");
        compras.registrar(cot.getId(),"OC-CU12-"+UUID.randomUUID().toString().substring(0,7),LocalDate.now(),cot.getTotal(),pdf("oc.pdf"),null);
        Empleado tecnico=empleados.findAll().stream().filter(e->"TECNICO".equals(e.getCargo().getNombre())).findFirst().orElseThrow();
        Empleado ssoma=empleados.findAll().stream().filter(e->"SSOMA".equals(e.getCargo().getNombre())).findFirst().orElseThrow();
        usuario("supervisor");
        OrdenTrabajo ot=asignaciones.registrar(new AsignacionPersonalRequestDTO(solicitud.getId(),tecnico.getId(),ssoma.getId(),null,List.of()));
        LocalDateTime inicio=LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        programacion.programar(ot.getId(),inicio,inicio.plusHours(2),"Sala de tableros",LocalDate.now(),true,
            pdf("pt.pdf"),pdf("iper.pdf"),null,null);
        return ot;
    }
    @Test void desdeAprobacionHastaConformidadYCambiosDeAlmacen(){
        OrdenTrabajo ot=prepararOT();
        usuario("ssoma");
        AnalisisTrabajoSeguro ats=analisis.elaborar(new AnalisisTrabajoSeguroRequestDTO(ot.getId(),"Área señalizada",
            List.of("Riesgo eléctrico"),List.of("Guantes"),"Multímetro",
            List.of(new AtsEtapaDTO("Inspección","Contacto eléctrico","Electrocución","ALTO","Desenergizar"))));
        usuario("almacenero");
        Material material=materialService.crear(new MaterialRequestDTO(
            "MAT-TEST-01","Cable de prueba","METRO",new BigDecimal("10"),new BigDecimal("2")));
        materialService.registrar(new MovimientoMaterialRequestDTO(material.getId(),ot.getId(),"SALIDA",
            new BigDecimal("3"),"Instalación","Entrega a cuadrilla"));
        HerramientaEquipo herramienta=herramientaService.crear(new HerramientaRequestDTO(
            "HER-TEST-01","Pinza amperimétrica","Prueba",new BigDecimal("4")));
        Empleado tecnico=empleados.findAll().stream().filter(e->"TECNICO".equals(e.getCargo().getNombre())).findFirst().orElseThrow();
        herramientaService.registrar(new MovimientoHerramientaRequestDTO(herramienta.getId(),ot.getId(),tecnico.getId(),
            "PRESTAMO",new BigDecimal("2"),null,null));
        assertEquals(0,herramienta.getCantidadDisponible().compareTo(new BigDecimal("2")));
        usuario("supervisor");
        EjecucionServicioRequestDTO datos=new EjecucionServicioRequestDTO(ot.getId(),"Pruebas de cableado", "220 V","Sin incidentes",
            List.of(new MaterialRealDTO(material.getId(),new BigDecimal("3"))));
        assertThrows(NegocioException.class,()->ejecuciones.registrar(datos,foto("antes.png"),null,null));
        aprobaciones.registrar(ats.getId(),new AprobacionAtsRequestDTO("Supervisor de cliente","SSOMA de cliente",
            LocalDate.now(),"APROBADO","Firmas externas comprobadas"),pdf("acta-ats.pdf"));
        assertEquals("APROBADO",atsRepo.findById(ats.getId()).orElseThrow().getEstado());
        EjecucionServicio eje=ejecuciones.registrar(datos,foto("antes.png"),null,null);
        assertEquals("REGISTRADA",eje.getEstado());
        assertEquals("VALIDADO",declarados.findByEjecucionServicioId(eje.getId()).get(0).getEstadoValidacion());
        LocalDate fechaEnvioInforme = ot.getFechaProgramadaFin().toLocalDate().plusDays(1);
        InformeTecnico informe=informes.registrar(new InformeTecnicoRequestDTO(ot.getId(),null,"Inspección realizada",
            "Revisión y mediciones","Cumple especificación","Continuar mantenimiento",fechaEnvioInforme),
            pdf("informe.pdf"),List.of());
        assertEquals("ENVIADO",informe.getEstado());
        ConformidadServicio conf=conformidades.registrar(new ConformidadServicioRequestDTO(
            ot.getId(),fechaEnvioInforme,"Responsable cliente",null,5,"CONFORME","Sin observaciones"),pdf("acta-conformidad.pdf"));
        assertEquals("CONFORME",conf.getEstadoConformidad());
        assertTrue(conf.getNumeroActa().matches("ACTA-\\d{4}-\\d{3,}"));
        assertEquals("ATENDIDA",ordenes.findById(ot.getId()).orElseThrow().getEstado());
        usuario("almacenero");
        herramientaService.registrar(new MovimientoHerramientaRequestDTO(herramienta.getId(),ot.getId(),tecnico.getId(),
            "DEVOLUCION",BigDecimal.ONE,"BUENO",null));
        assertEquals(0,herramienta.getCantidadDisponible().compareTo(new BigDecimal("3")));
        assertThrows(NegocioException.class,()->materialService.registrar(new MovimientoMaterialRequestDTO(
            material.getId(),ot.getId(),"SALIDA",new BigDecimal("99"),null,null)));
    }
}
