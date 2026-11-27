package pe.edu.sigestec.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ArchivoStorageService {
    String guardar(MultipartFile archivo, boolean obligatorio, boolean soloPdf);

    Resource cargar(String url);

    String tipoContenido(String url);
}
