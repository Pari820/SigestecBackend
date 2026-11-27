package pe.edu.sigestec.entity.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import jakarta.persistence.*;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.sigestec.entity.catalogos.PersonaNatural;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "USUARIO")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_natural", nullable = false)
    private PersonaNatural personaNatural;
    @Column(name = "nombre_usuario", length = 60)
    private String nombreUsuario;
    @Column(name = "clave_hash", length = 255)
    private String claveHash;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";

    @Column(name = "token_version")
    @Setter(AccessLevel.NONE)
    private Long tokenVersion = 0L;

    public long getTokenVersion() { return tokenVersion == null ? 0L : tokenVersion; }

    public void invalidarSesiones() { tokenVersion = getTokenVersion() + 1L; }

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
