package pe.edu.sigestec.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.edu.sigestec.exception.NegocioException;

@Service
public class ConsultaSunatService {
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json;
    private final String clave;

    public record Empresa(String ruc, String razonSocial, String estado, String condicion, String direccion) {}

    public ConsultaSunatService(ObjectMapper json, @Value("${app.sunat.api-token:}") String clave) {
        this.json = json;
        this.clave = clave;
    }

    public List<Empresa> consultar(String entrada) {
        if (clave == null || clave.isBlank())
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, "La consulta de empresas requiere configurar SUNAT_API_TOKEN.");
        String valor = entrada == null ? "" : entrada.trim();
        boolean ruc = valor.matches("\\d+");
        if (ruc && !valor.matches("\\d{11}"))
            throw NegocioException.invalido("El RUC debe contener exactamente 11 dígitos.");
        if (!ruc && (valor.length() < 3 || valor.length() > 80))
            throw NegocioException.invalido("Escribe al menos 3 caracteres de la razón social.");
        String ruta = ruc ? "/v1/ruc/" + valor : "/v1/empresas?q=" + URLEncoder.encode(valor, StandardCharsets.UTF_8);
        try {
            HttpRequest solicitud = HttpRequest.newBuilder(URI.create("https://api.endpoint.pe" + ruta))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + clave)
                    .GET().build();
            HttpResponse<String> respuesta = cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() == 404) return List.of();
            if (respuesta.statusCode() == 401 || respuesta.statusCode() == 403)
                throw new NegocioException(HttpStatus.BAD_GATEWAY, "El proveedor de SUNAT rechazó la clave de consulta.");
            if (respuesta.statusCode() == 402 || respuesta.statusCode() == 429)
                throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, "Se alcanzó el límite de consultas del proveedor.");
            if (respuesta.statusCode() >= 400)
                throw new NegocioException(HttpStatus.BAD_GATEWAY, "No se pudo consultar el padrón de contribuyentes.");
            JsonNode raiz = json.readTree(respuesta.body());
            if (!raiz.path("success").asBoolean(false))
                throw new NegocioException(HttpStatus.BAD_GATEWAY, "El proveedor no devolvió información válida.");
            JsonNode datos = raiz.path("data");
            if (ruc) return List.of(convertir(datos));
            List<Empresa> empresas = new ArrayList<>();
            JsonNode resultados = datos.path("resultados");
            if (resultados.isArray()) for (JsonNode elemento : resultados) empresas.add(convertir(elemento));
            return empresas;
        } catch (NegocioException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE, "La consulta fue interrumpida. Inténtalo nuevamente.");
        } catch (Exception e) {
            throw new NegocioException(HttpStatus.BAD_GATEWAY, "No fue posible conectar con la consulta de empresas.");
        }
    }

    private Empresa convertir(JsonNode n) {
        return new Empresa(n.path("ruc").asText(""), n.path("razon_social").asText(""),
                n.path("estado").asText(""), n.path("condicion").asText(""),
                n.path("direccion_completa").asText(n.path("direccion").asText("")));
    }
}
