package pe.edu.sigestec.entity.almacen;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.sigestec.entity.orden_trabajo.OrdenTrabajo;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MOVIMIENTO_MATERIAL_ALMACEN")
public class MovimientoMaterialAlmacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento_material_almacen")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_orden_trabajo")
    private OrdenTrabajo ordenTrabajo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;

    @Column(name = "tipo_movimiento", length = 20)
    private String tipoMovimiento;

    @Column(name = "cantidad", precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    @Column(name = "motivo", length = 400)
    private String motivo;

    @Column(name = "observacion", length = 1000)
    private String observacion;
}
