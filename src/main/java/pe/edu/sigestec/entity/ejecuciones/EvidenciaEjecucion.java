package pe.edu.sigestec.entity.ejecuciones;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "EVIDENCIA_EJECUCION")
public class EvidenciaEjecucion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_ejecucion")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_ejecucion_servicio", nullable = false)
    private EjecucionServicio ejecucionServicio;

    @Column(name = "etapa", length = 20)
    private String etapa;

    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;

    @Column(name = "nombre_archivo", length = 250)
    private String nombreArchivo;
}
