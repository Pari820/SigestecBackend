package pe.edu.sigestec.entity.ejecuciones;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.sigestec.entity.almacen.Material;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "MATERIAL_UTILIZADO")
public class MaterialUtilizado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material_utilizado")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_ejecucion_servicio", nullable = false)
    private EjecucionServicio ejecucionServicio;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @Column(name = "cantidad_real", precision = 12, scale = 2)
    private BigDecimal cantidadReal;

    @Column(name = "estado_validacion", length = 25)
    private String estadoValidacion = "PENDIENTE";
}
