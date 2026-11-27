package pe.edu.sigestec.entity.ats;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ANALISIS_TRABAJO_SEGURO")
public class AnalisisTrabajoSeguro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis_trabajo_seguro")
    private Long id;
    @Column(name = "numero_ats", length = 30, nullable = false)
    private String numeroAts;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_elabora", nullable = false)
    private Empleado empleadoElabora;
    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro = LocalDate.now();
    @Column(name = "condiciones_area", length = 1500)
    private String condicionesArea;
    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;
}
