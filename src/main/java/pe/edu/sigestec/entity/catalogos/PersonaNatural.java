package pe.edu.sigestec.entity.catalogos;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.sigestec.entity.auth.Perfil;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "PERSONA_NATURAL")
public class PersonaNatural {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona_natural")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;
    @Column(name = "dni", length = 8)
    private String dni;
    @Column(name = "nombres", length = 100)
    private String nombres;
    @Column(name = "apellidos", length = 120)
    private String apellidos;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "correo", length = 150)
    private String correo;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
