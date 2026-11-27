package pe.edu.sigestec.entity.catalogos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TIPO_SERVICIO")
public class TipoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_servicio")
    private Long id;

    /** Número visible correlativo; no sustituye la clave primaria. */
    @Column(name = "numero_registro")
    private Long numeroRegistro;
    @Column(name = "nombre", length = 120)
    private String nombre;
    @Column(name = "descripcion", length = 1000)
    private String descripcion;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
