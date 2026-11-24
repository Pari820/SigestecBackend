package pe.edu.sigestec.entity.compras;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.cotizaciones.Cotizacion;
import pe.edu.sigestec.entity.personal.Empleado;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ORDEN_COMPRA")
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_compra")
    private Long id;
    @Column(name = "codigo_interno", length = 30, nullable = false)
    private String codigoInterno;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_cotizacion", nullable = false)
    private Cotizacion cotizacion;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_representante_emisor", nullable = true)
    private RepresentanteEmpresa representanteEmisor;
    @Column(name = "numero_orden_compra", length = 40)
    private String numeroOrdenCompra;
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;
    @Column(name = "monto_total", precision = 12, scale = 2)
    private BigDecimal montoTotal;
    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;
    @Column(name = "estado", length = 20)
    private String estado = "REGISTRADA";
}
