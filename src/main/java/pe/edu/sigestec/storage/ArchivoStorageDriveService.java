package pe.edu.sigestec.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.sigestec.exception.NegocioException;

@Service
@Primary
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "drive")
public class ArchivoStorageDriveService implements ArchivoStorageService {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final ObjectMapper json;
    private final ArchivoStorageLocalService archivosAnteriores;
    private final String idCliente;
    private final String secretoCliente;
    private final String refreshToken;
    private final String carpeta;
    private final String urlBase;

    public ArchivoStorageDriveService(ObjectMapper json, ArchivoStorageLocalService archivosAnteriores,
            @Value("${app.storage.drive.client-id:}") String idCliente,
            @Value("${app.storage.drive.client-secret:}") String secretoCliente,
            @Value("${app.storage.drive.refresh-token:}") String refreshToken,
            @Value("${app.storage.drive.folder-id:}") String carpeta,
            @Value("${app.storage.public-base-url:http://localhost:8080}") String urlBase) {
        this.json = json;
        this.archivosAnteriores = archivosAnteriores;
        this.idCliente = idCliente;
        this.secretoCliente = secretoCliente;
        this.refreshToken = refreshToken;
        this.carpeta = carpeta;
        this.urlBase = urlBase.replaceAll("/+$", "");
    }

    private String tokenAcceso() {
        if (idCliente.isBlank() || secretoCliente.isBlank() || refreshToken.isBlank() || carpeta.isBlank())
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Google Drive requiere DRIVE_CLIENT_ID, DRIVE_CLIENT_SECRET, DRIVE_REFRESH_TOKEN y DRIVE_FOLDER_ID.");
        String datos = "client_id=" + codificar(idCliente) + "&client_secret=" + codificar(secretoCliente)
                + "&refresh_token=" + codificar(refreshToken) + "&grant_type=refresh_token";
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) throw new IOException("Error OAuth: " + res.statusCode());
            String token = json.readTree(res.body()).path("access_token").asText();
            if (token.isBlank()) throw new IOException("Sin token OAuth");
            return token;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new NegocioException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo autorizar Google Drive. Revisa el consentimiento OAuth y las credenciales.");
        }
    }

    @Override
    public String guardar(MultipartFile archivo, boolean obligatorio, boolean soloPdf) {
        if (archivo == null || archivo.isEmpty()) {
            if (obligatorio) throw NegocioException.invalido("Debes adjuntar un archivo.");
            return null;
        }
        if (archivo.getSize() > 8L * 1024 * 1024) throw NegocioException.invalido("El archivo supera 8 MB.");
        try {
            byte[] contenido = archivo.getBytes();
            String original = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase(Locale.ROOT);
            String ext = contenido.length >= 5 && new String(contenido, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")
                    && original.endsWith(".pdf") ? "pdf" : null;
            if (!soloPdf && contenido.length >= 4 && (contenido[0] & 255) == 137 && (contenido[1] & 255) == 80
                    && original.endsWith(".png")) ext = "png";
            if (!soloPdf && contenido.length >= 3 && (contenido[0] & 255) == 255 && (contenido[1] & 255) == 216
                    && (original.endsWith(".jpg") || original.endsWith(".jpeg"))) ext = "jpg";
            if (ext == null) throw NegocioException.invalido("Adjunta un PDF" + (soloPdf ? "" : ", PNG o JPG") + " válido.");
            String mime = ext.equals("pdf") ? "application/pdf" : ext.equals("png") ? "image/png" : "image/jpeg";
            String nombre = "SIGESTEC-" + UUID.randomUUID() + "." + ext;
            String boundary = "sigestec-" + UUID.randomUUID();
            String meta = json.createObjectNode().put("name", nombre).put("mimeType", mime)
                    .set("parents", json.createArrayNode().add(carpeta)).toString();
            String inicio = "--" + boundary + "\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n"
                    + meta + "\r\n--" + boundary + "\r\nContent-Type: " + mime + "\r\n\r\n";
            String fin = "\r\n--" + boundary + "--\r\n";
            byte[] a = inicio.getBytes(StandardCharsets.UTF_8);
            byte[] z = fin.getBytes(StandardCharsets.UTF_8);
            byte[] body = new byte[a.length + contenido.length + z.length];
            System.arraycopy(a, 0, body, 0, a.length);
            System.arraycopy(contenido, 0, body, a.length, contenido.length);
            System.arraycopy(z, 0, body, a.length + contenido.length, z.length);
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id"))
                    .timeout(Duration.ofSeconds(35))
                    .header("Authorization", "Bearer " + tokenAcceso())
                    .header("Content-Type", "multipart/related; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200 && res.statusCode() != 201) throw new IOException("Error Drive " + res.statusCode());
            String id = json.readTree(res.body()).path("id").asText();
            if (!id.matches("[A-Za-z0-9_-]{10,200}")) throw new IOException("No se obtuvo un ID de Drive válido");
            return urlBase + "/api/v1/archivos/gdrive-" + id + "." + ext;
        } catch (NegocioException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new NegocioException(HttpStatus.BAD_GATEWAY,
                    "No se pudo subir el archivo a Google Drive. El registro no debe confirmarse.");
        }
    }

    @Override
    public Resource cargar(String url) {
        if (!esDrive(url)) return archivosAnteriores.cargar(url);
        String id = idDrive(url);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://www.googleapis.com/drive/v3/files/" + id + "?alt=media"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + tokenAcceso()).GET().build();
            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (res.statusCode() == 404) throw NegocioException.noExiste("Documento no encontrado en Drive.");
            if (res.statusCode() != 200 || res.body().length > 12 * 1024 * 1024)
                throw new IOException("No se pudo descargar el documento");
            return new ByteArrayResource(res.body());
        } catch (NegocioException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new NegocioException(HttpStatus.BAD_GATEWAY, "No fue posible descargar el documento desde Drive.");
        }
    }

    @Override
    public String tipoContenido(String url) {
        if (!esDrive(url)) return archivosAnteriores.tipoContenido(url);
        idDrive(url);
        String file = url.substring(url.lastIndexOf('/') + 1);
        return file.endsWith(".pdf") ? "application/pdf" : file.endsWith(".png") ? "image/png" : "image/jpeg";
    }

    private static boolean esDrive(String url) {
        return url != null && url.substring(url.lastIndexOf('/') + 1).startsWith("gdrive-");
    }

    private static String idDrive(String url) {
        String name = url == null ? "" : url.substring(url.lastIndexOf('/') + 1);
        if (!name.matches("gdrive-[A-Za-z0-9_-]{10,200}\\.(pdf|png|jpg)"))
            throw NegocioException.invalido("Referencia de documento Drive inválida.");
        return name.substring("gdrive-".length(), name.lastIndexOf('.'));
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
