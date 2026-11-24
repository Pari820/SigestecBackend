package pe.edu.sigestec.entity.orden_trabajo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DOCUMENTO_PLANIFICACION")
public class DocumentoPlanificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento_planificacion")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @Column(name = "tipo_documento", length = 30)
    private String tipoDocumento;
    @Column(name = "nombre_documento", length = 200)
    private String nombreDocumento;
    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
    @Column(name = "fecha_envio_cliente")
    private LocalDateTime fechaEnvioCliente;
    @Column(name = "estado", length = 20)
    private String estado = "REGISTRADO";
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
}
