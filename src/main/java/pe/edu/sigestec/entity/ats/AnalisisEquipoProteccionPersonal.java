package pe.edu.sigestec.entity.ats;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ANALISIS_EQUIPO_PROTECCION_PERSONAL")
public class AnalisisEquipoProteccionPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis_equipo_proteccion")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_analisis_trabajo_seguro", nullable = false)
    private AnalisisTrabajoSeguro analisisTrabajoSeguro;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_equipo_proteccion_personal", nullable = false)
    private EquipoProteccionPersonal equipoProteccionPersonal;
    @Column(name = "cantidad", precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
}
