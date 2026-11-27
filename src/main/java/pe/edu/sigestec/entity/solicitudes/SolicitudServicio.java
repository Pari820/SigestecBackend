package pe.edu.sigestec.entity.solicitudes;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.catalogos.TipoServicio;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SOLICITUD_SERVICIO")
public class SolicitudServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;
    @Column(name = "codigo_solicitud", length = 30, nullable = false)
    private String codigoSolicitud;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_representante_solicitante", nullable = true)
    private RepresentanteEmpresa representanteSolicitante;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_servicio", nullable = false)
    private TipoServicio tipoServicio;
    @Column(name = "fecha_solicitud")
    private LocalDate fechaSolicitud = LocalDate.now();
    @Column(name = "fecha_requerida")
    private LocalDate fechaRequerida;
    @Column(name = "descripcion", length = 1000)
    private String descripcion;
    @Column(name = "lugar_servicio", length = 300)
    private String lugarServicio;
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    @Column(name = "prioridad", length = 255)
    private String prioridad = "MEDIA";
    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";
    @Column(name = "url_archivo_adjunto", length = 1000)
    private String urlArchivoAdjunto;
}
