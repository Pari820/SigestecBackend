package pe.edu.sigestec.entity.orden_trabajo;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.compras.OrdenCompra;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ORDEN_TRABAJO")
public class OrdenTrabajo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_trabajo")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_compra", nullable = false)
    private OrdenCompra ordenCompra;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @Column(name = "numero_orden_trabajo", length = 30)
    private String numeroOrdenTrabajo;
    @Column(name = "fecha_programada_inicio")
    private LocalDateTime fechaProgramadaInicio;
    @Column(name = "fecha_programada_fin")
    private LocalDateTime fechaProgramadaFin;
    @Column(name = "fecha_coordinacion_cliente")
    private LocalDateTime fechaCoordinacionCliente;
    @Column(name = "lugar_trabajo", length = 300)
    private String lugarTrabajo;
    @Column(name = "descripcion", length = 1000)
    private String descripcion;
    @Column(name = "estado", length = 25)
    private String estado = "PENDIENTE";
}
