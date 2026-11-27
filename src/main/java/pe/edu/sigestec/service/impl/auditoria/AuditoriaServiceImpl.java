package pe.edu.sigestec.service.impl.auditoria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.sigestec.auditoria.EventoAuditoria;
import pe.edu.sigestec.dto.auditoria.AuditoriaEventoDTO;
import pe.edu.sigestec.dto.auditoria.PaginaAuditoriaDTO;
import pe.edu.sigestec.entity.auditoria.AuditoriaEvento;
import pe.edu.sigestec.exception.NegocioException;
import pe.edu.sigestec.repository.auditoria.AuditoriaEventoRepository;
import pe.edu.sigestec.service.service.auditoria.AuditoriaService;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {
    private final AuditoriaEventoRepository repository;
    public AuditoriaServiceImpl(AuditoriaEventoRepository repository) { this.repository = repository; }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(EventoAuditoria evento) {
        AuditoriaEvento a = new AuditoriaEvento();
        a.setFecha(LocalDateTime.now());
        a.setUsuario(evento.usuario() == null ? "sistema" : evento.usuario());
        a.setAccion(evento.accion());
        a.setEntidad(evento.entidad());
        a.setEntidadId(evento.entidadId());
        a.setDetalle(evento.detalle());
        a.setRequestId(evento.requestId());
        repository.save(a);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaAuditoriaDTO buscar(String usuario, String accion,
                                     LocalDate desde, LocalDate hasta, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > 100)
            throw NegocioException.invalido("Página inválida o tamaño fuera de 1 a 100");
        if (desde != null && hasta != null && desde.isAfter(hasta))
            throw NegocioException.invalido("El rango de fechas es inválido");

        Specification<AuditoriaEvento> filtro = (root, query, cb) -> cb.conjunction();
        if (usuario != null && !usuario.isBlank()) {
            String valor = usuario.trim().toUpperCase(java.util.Locale.ROOT);
            filtro = filtro.and((root, query, cb) -> cb.equal(cb.upper(root.get("usuario")), valor));
        }
        if (accion != null && !accion.isBlank()) {
            String valor = accion.trim().toUpperCase(java.util.Locale.ROOT);
            filtro = filtro.and((root, query, cb) -> cb.equal(root.get("accion"), valor));
        }
        if (desde != null) {
            filtro = filtro.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDateTime>get("fecha"), desde.atStartOfDay()));
        }
        if (hasta != null) {
            filtro = filtro.and((root, query, cb) -> cb.lessThan(root.<LocalDateTime>get("fecha"), hasta.plusDays(1).atStartOfDay()));
        }
        Page<AuditoriaEventoDTO> p = repository.findAll(filtro,
                PageRequest.of(pagina, tamanio, Sort.by(Sort.Direction.DESC, "fecha")
                        .and(Sort.by(Sort.Direction.DESC, "id"))))
                .map(AuditoriaEventoDTO::de);
        return new PaginaAuditoriaDTO(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}
