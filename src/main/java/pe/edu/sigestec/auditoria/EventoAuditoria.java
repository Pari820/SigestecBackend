package pe.edu.sigestec.auditoria;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Evento sin datos sensibles; se publica dentro de la transacción de negocio. */
public record EventoAuditoria(String usuario, String accion, String entidad,
                              Long entidadId, String detalle, String requestId) {
    public static EventoAuditoria de(String accion, String entidad, Long entidadId, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                ? "sistema" : auth.getName();
        return new EventoAuditoria(recortar(usuario, 60), recortar(accion, 50),
                recortar(entidad, 60), entidadId, recortar(detalle, 500), MDC.get("requestId"));
    }

    public static EventoAuditoria login(String accion, String nombreUsuario) {
        return new EventoAuditoria(recortar(nombreUsuario == null ? "desconocido" : nombreUsuario, 60),
                accion, "Usuario", null, null, MDC.get("requestId"));
    }

    private static String recortar(String valor, int max) {
        return valor == null || valor.length() <= max ? valor : valor.substring(0, max);
    }
}
