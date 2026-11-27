package pe.edu.sigestec.entity.ats;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DETALLE_ANALISIS_RIESGO")
public class DetalleAnalisisRiesgo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_analisis_riesgo")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_analisis_trabajo_seguro", nullable = false)
    private AnalisisTrabajoSeguro analisisTrabajoSeguro;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_riesgo", nullable = false)
    private Riesgo riesgo;
    @Column(name = "etapa_trabajo", length = 300)
    private String etapaTrabajo;
    @Column(name = "actividad", length = 300)
    private String actividad;
    @Column(name = "peligro", length = 300)
    private String peligro;
    @Column(name = "medida_control", length = 500)
    private String medidaControl;
    @Column(name = "probabilidad")
    private Integer probabilidad;
    @Column(name = "severidad")
    private Integer severidad;
    @Column(name = "nivel_riesgo", length = 30)
    private String nivelRiesgo;
}
