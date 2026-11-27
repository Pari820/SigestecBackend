package pe.edu.sigestec.entity.cotizaciones;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import pe.edu.sigestec.entity.catalogos.PersonaJuridica;
import pe.edu.sigestec.entity.catalogos.RepresentanteEmpresa;
import pe.edu.sigestec.entity.personal.Empleado;
import pe.edu.sigestec.entity.solicitudes.SolicitudServicio;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "COTIZACION")
public class Cotizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private SolicitudServicio solicitudServicio;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_persona_juridica", nullable = false)
    private PersonaJuridica empresaCliente;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_empleado_registra", nullable = false)
    private Empleado empleadoRegistra;
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "id_representante_respuesta", nullable = true)
    private RepresentanteEmpresa representanteRespuesta;
    @Column(name = "numero_cotizacion", length = 30)
    private String numeroCotizacion;
    @Column(name = "fecha_emision")
    private LocalDate fechaEmision = LocalDate.now();
    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;
    @Column(name = "fecha_respuesta")
    private LocalDate fechaRespuesta;
    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "igv", precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;
    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    @Column(name = "estado", length = 20)
    private String estado = "BORRADOR";
    @Column(name = "observaciones", length = 1000)
    private String observaciones;
    @Column(name = "url_archivo", length = 1000)
    private String urlArchivo;
}
