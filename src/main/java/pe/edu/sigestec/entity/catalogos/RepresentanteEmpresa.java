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
@Table(name = "REPRESENTANTE_EMPRESA")
public class RepresentanteEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_representante")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_natural", nullable = false)
    private PersonaNatural personaNatural;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica personaJuridica;
    @Column(name = "cargo_representante", length = 120)
    private String cargoRepresentante;
    @Column(name = "tipo_representante", length = 30)
    private String tipoRepresentante = "CONTACTO";
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "estado", length = 1, columnDefinition = "CHAR(1)")
    private String estado = "A";
}
