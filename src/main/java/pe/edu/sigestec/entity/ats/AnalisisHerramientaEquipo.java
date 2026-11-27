package pe.edu.sigestec.entity.ats;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.sigestec.entity.almacen.HerramientaEquipo;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ANALISIS_HERRAMIENTA_EQUIPO")
public class AnalisisHerramientaEquipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_analisis_herramienta_equipo")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_analisis_trabajo_seguro", nullable = false)
    private AnalisisTrabajoSeguro analisisTrabajoSeguro;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_herramienta_equipo", nullable = false)
    private HerramientaEquipo herramientaEquipo;
    @Column(name = "cantidad_requerida", precision = 12, scale = 2)
    private BigDecimal cantidadRequerida = BigDecimal.ONE;
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
}
