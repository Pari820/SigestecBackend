package pe.edu.sigestec.entity.conformidades;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.informes.InformeTecnico;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "CONFORMIDAD_SERVICIO")
public class ConformidadServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conformidad_servicio")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_informe_tecnico", nullable = false)
    private InformeTecnico informeTecnico;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_representante_empresa")
    private RepresentanteEmpresa representanteEmpresa;

    @Column(name = "nombre_representante", length = 250)
    private String nombreRepresentante;

    @Column(name = "numero_acta", length = 50)
    private String numeroActa;

    @Column(name = "fecha_conformidad")
    private LocalDate fechaConformidad;

    @Column(name = "valoracion")
    private Integer valoracion;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Column(name = "estado_conformidad", length = 30)
    private String estadoConformidad;

    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;
}
