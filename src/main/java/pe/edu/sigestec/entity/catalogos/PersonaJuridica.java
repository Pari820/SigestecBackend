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
@Table(name = "PERSONA_JURIDICA")
public class PersonaJuridica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona_juridica")
    private Long id;

    /** Número visible correlativo; no sustituye la clave primaria. */
    @Column(name = "numero_registro")
    private Long numeroRegistro;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_perfil", nullable = false)
    private Perfil perfil;
    @Column(name = "ruc", length = 11)
    private String ruc;
    @Column(name = "razon_social", length = 180)
    private String razonSocial;
    @Column(name = "direccion", length = 250)
    private String direccion;
    @Column(name = "telefono", length = 20)
    private String telefono;
    @Column(name = "correo", length = 150)
    private String correo;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
