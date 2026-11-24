package pe.edu.sigestec.entity.informes;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.ejecuciones.EjecucionServicio;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "INFORME_TECNICO")
public class InformeTecnico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_informe_tecnico")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_ejecucion_servicio", nullable = false)
    private EjecucionServicio ejecucionServicio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;

    @Column(name = "numero_informe", length = 40)
    private String numeroInforme;

    @Column(name = "fecha_informe")
    private LocalDate fechaInforme = LocalDate.now();

    @Column(name = "fecha_envio")
    private LocalDate fechaEnvio;

    @Column(name = "diagnostico", length = 1500)
    private String diagnostico;

    @Column(name = "trabajos_realizados", length = 2000)
    private String trabajosRealizados;

    @Column(name = "conclusiones", length = 1500)
    private String conclusiones;

    @Column(name = "recomendaciones", length = 1500)
    private String recomendaciones;

    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;

    @Column(name = "estado", length = 25)
    private String estado = "REGISTRADA";
}
