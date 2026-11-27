package pe.edu.sigestec.entity.compras;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DETALLE_ORDEN_COMPRA")
public class DetalleOrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_orden_compra")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_orden_compra", nullable = false)
    private OrdenCompra ordenCompra;
    @Column(name = "item")
    private Integer item;
    @Column(name = "descripcion", length = 500)
    private String descripcion;
    @Column(name = "cantidad", precision = 12, scale = 2)
    private BigDecimal cantidad;
    @Column(name = "unidad_medida", length = 30)
    private String unidadMedida;
    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;
}
