package pe.edu.sigestec.entity.personal;

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
@Table(name = "CARGO")
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cargo")
    private Long id;
    @Column(name = "nombre", length = 80)
    private String nombre;
    @Column(name = "descripcion", length = 1000)
    private String descripcion;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
