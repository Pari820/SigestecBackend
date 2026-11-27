package pe.edu.sigestec.entity.auth;

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
@Table(name = "PERFIL")
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long id;
    @Column(name = "tipo_perfil", length = 10)
    private String tipoPerfil;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
