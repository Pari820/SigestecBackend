package pe.edu.sigestec.entity.ats;

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
@Table(name = "RIESGO")
public class Riesgo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_riesgo")
    private Long id;
    @Column(name = "nombre", length = 120)
    private String nombre;
    @Column(name = "descripcion", length = 300)
    private String descripcion;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
