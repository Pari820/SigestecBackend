package pe.edu.sigestec.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.edu.sigestec.exception.NegocioException;

@Service
public class ConsultaDniService {
    private final HttpClient cliente;
    private final ObjectMapper json;
    private final String clave;

    public record PersonaDni(String dni, String nombres, String apellidoPaterno, String apellidoMaterno) {}

    @Autowired
    public ConsultaDniService(ObjectMapper json, @Value("${app.identidad.api-token:}") String clave) {
        this(json, clave, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }

    ConsultaDniService(ObjectMapper json, String clave, HttpClient cliente) {
        this.json = json;
        this.clave = clave;
        this.cliente = cliente;
    }

    public PersonaDni consultar(String dni) {
        if (dni == null || !dni.matches("[0-9]{8}")) {
            throw NegocioException.invalido("El DNI debe tener exactamente 8 dígitos.");
        }
        if (clave == null || clave.isBlank()) {
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La consulta de DNI requiere configurar IDENTIDAD_API_TOKEN o SUNAT_API_TOKEN en el backend.");
        }
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create("https://api.endpoint.pe/v1/dni/" + dni))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + clave)
                    .GET()
                    .build();
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            int estado = respuesta.statusCode();
            if (estado == 404) {
                throw NegocioException.noExiste("No se encontraron datos para el DNI indicado.");
            }
            if (estado == 401 || estado == 403) {
                throw new NegocioException(HttpStatus.BAD_GATEWAY, "El proveedor rechazó la clave de consulta de DNI.");
            }
            if (estado == 402 || estado == 429) {
                throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Se alcanzó el límite de consultas DNI. Puedes ingresar el personal manualmente.");
            }
            if (estado >= 400) {
                throw new NegocioException(HttpStatus.BAD_GATEWAY,
                        "El proveedor de DNI no pudo procesar la consulta. Inténtalo más tarde.");
            }
            return interpretar(dni, respuesta.body());
        } catch (NegocioException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "La consulta se interrumpió. Puedes registrar el personal manualmente.");
        } catch (Exception e) {
            throw new NegocioException(HttpStatus.BAD_GATEWAY,
                    "No fue posible conectar con el servicio de DNI. Puedes registrar el personal manualmente.");
        }
    }

    PersonaDni interpretar(String dni, String respuesta) throws java.io.IOException {
            JsonNode raiz = json.readTree(respuesta);
            if (!raiz.path("success").asBoolean(false)) {
                throw new NegocioException(HttpStatus.BAD_GATEWAY,
                        "No se obtuvo información válida para ese DNI.");
            }
            JsonNode datos = raiz.path("data");
            String encontrado = datos.path("dni").asText("").trim();
            String nombres = datos.path("nombres").asText("").trim();
            String paterno = datos.path("apellido_paterno").asText("").trim();
            String materno = datos.path("apellido_materno").asText("").trim();
            if (!dni.equals(encontrado) || nombres.isBlank() || paterno.isBlank()) {
                throw new NegocioException(HttpStatus.BAD_GATEWAY,
                        "La consulta no devolvió datos de identidad completos y coincidentes.");
            }
            return new PersonaDni(encontrado, nombres, paterno, materno);
    }
}
