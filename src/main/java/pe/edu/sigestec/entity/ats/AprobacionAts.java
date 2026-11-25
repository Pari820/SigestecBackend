package pe.edu.sigestec.entity.ats;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "APROBACION_ATS")
public class AprobacionAts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aprobacion_ats")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_analisis_trabajo_seguro", nullable = false)
    private AnalisisTrabajoSeguro analisisTrabajoSeguro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;

    @Column(name = "fecha_revision")
    private LocalDate fechaRevision = LocalDate.now();

    @Column(name = "supervisor_cliente", length = 300)
    private String supervisorCliente;

    @Column(name = "ssoma_cliente", length = 300)
    private String ssomaCliente;

    @Column(name = "resultado", length = 20)
    private String resultado;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "url_acta", length = 1000)
    private String urlActa;
}
