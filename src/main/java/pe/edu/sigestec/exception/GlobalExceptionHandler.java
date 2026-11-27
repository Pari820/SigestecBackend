package pe.edu.sigestec.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import pe.edu.sigestec.dto.common.ErrorResponseDTO;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Centraliza respuestas HTTP, manteniendo los status y el campo "mensaje" de SIGESTEC. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponseDTO> negocio(NegocioException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        return respuesta(status, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validacion(MethodArgumentNotValidException ex,
                                                        HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.putIfAbsent(error.getField(), error.getDefaultMessage()));
        // Mantener el mismo mensaje destacado que el backend anterior.
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst().orElse("Datos incorrectos");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, request, campos);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> archivoGrande(MaxUploadSizeExceededException ex,
                                                            HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "Archivo excede el tamaño permitido (8 MB por archivo)", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> parametro(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "Tipo de dato inválido", request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> cuerpoInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no tiene un formato válido", request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> autenticacion(AuthenticationException ex,
                                                            HttpServletRequest request) {
        LOG.warn("Autenticación fallida en {} ({})", request.getRequestURI(), ex.getClass().getSimpleName());
        return respuesta(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> sinPermiso(AccessDeniedException ex,
                                                        HttpServletRequest request) {
        LOG.warn("Acceso denegado a {}", request.getRequestURI());
        return respuesta(HttpStatus.FORBIDDEN,
                "No tiene permisos para realizar esta operación", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> integridad(DataIntegrityViolationException ex,
                                                        HttpServletRequest request) {
        LOG.warn("Restricción de integridad en {}", request.getRequestURI());
        return respuesta(HttpStatus.CONFLICT,
                "El registro viola una restricción de integridad o unicidad", request, null);
    }

    /**
     * Evita que una excepción no prevista termine en el despacho Servlet /error,
     * donde podría confundirse con un problema de autenticación. El detalle completo
     * queda en el log del backend; al cliente se devuelve un mensaje seguro.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> inesperado(Exception ex, HttpServletRequest request) {
        LOG.error("Error no controlado al procesar {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno al procesar la solicitud. Revisa la consola del backend.", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(HttpStatus status, String mensaje,
                                                        HttpServletRequest request,
                                                        Map<String, String> erroresValidacion) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                Instant.now().toString(), status.value(), status.getReasonPhrase(), mensaje,
                request.getRequestURI(), erroresValidacion, MDC.get("requestId"));
        return ResponseEntity.status(status).body(body);
    }
}
