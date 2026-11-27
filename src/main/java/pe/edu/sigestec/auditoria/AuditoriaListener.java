package pe.edu.sigestec.auditoria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.edu.sigestec.service.service.auditoria.AuditoriaService;

/** Nunca interrumpe el proceso principal si falla la escritura de auditoría. */
@Component
public class AuditoriaListener {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);
    private final AuditoriaService service;

    public AuditoriaListener(AuditoriaService service) { this.service = service; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void despuesDeConfirmar(EventoAuditoria evento) {
        try { service.registrar(evento); }
        catch (RuntimeException ex) {
            log.error("No se pudo registrar auditoría {} {}", evento.accion(), evento.entidadId(), ex);
        }
    }
}
