package pe.edu.sigestec.entity.ejecuciones;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EJECUCION_SERVICIO")
public class EjecucionServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejecucion_servicio")
    private Long id;

    @Column(name = "numero_ejecucion", length = 30, nullable = false)
    private String numeroEjecucion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;

    @Column(name = "fecha_ejecucion")
    private LocalDateTime fechaEjecucion = LocalDateTime.now();

    @Column(name = "descripcion", length = 1500)
    private String descripcion;

    @Column(name = "mediciones", length = 1500)
    private String mediciones;

    @Column(name = "observaciones", length = 1500)
    private String observaciones;

    @Column(name = "estado", length = 30)
    private String estado = "REGISTRADA";
}
