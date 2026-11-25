package pe.edu.sigestec.entity.almacen;

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
@Table(name = "MATERIAL")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(name = "codigo", length = 30)
    private String codigo;

    @Column(name = "nombre", length = 180)
    private String nombre;

    @Column(name = "unidad_medida", length = 30)
    private String unidadMedida;

    @Column(name = "stock_actual", precision = 12, scale = 2)
    private BigDecimal stockActual = BigDecimal.ZERO;

    @Column(name = "stock_minimo", precision = 12, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Column(name = "estado", length = 1)
    private String estado = "A";
}
