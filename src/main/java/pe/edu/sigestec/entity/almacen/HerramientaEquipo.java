package pe.edu.sigestec.entity.almacen;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "HERRAMIENTA_EQUIPO")
public class HerramientaEquipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_herramienta_equipo")
    private Long id;
    @Column(name = "codigo_herramienta", length = 30)
    private String codigoHerramienta;
    @Column(name = "nombre", length = 180)
    private String nombre;
    @Column(name = "descripcion", length = 400)
    private String descripcion;
    @Column(name = "cantidad_total", precision = 12, scale = 2)
    private BigDecimal cantidadTotal = BigDecimal.ZERO;
    @Column(name = "cantidad_disponible", precision = 12, scale = 2)
    private BigDecimal cantidadDisponible = BigDecimal.ZERO;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
