package pe.edu.sigestec.entity.personal;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EMPLEADO")
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long id;

    /** Número visible correlativo; no sustituye la clave primaria. */
    @Column(name = "numero_registro")
    private Long numeroRegistro;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_natural", nullable = false)
    private PersonaNatural personaNatural;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_cargo", nullable = false)
    private Cargo cargo;
    @Column(name = "codigo_empleado", length = 20)
    private String codigoEmpleado;
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso = LocalDate.now();
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
