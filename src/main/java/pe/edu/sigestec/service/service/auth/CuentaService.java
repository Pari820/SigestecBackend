package pe.edu.sigestec.service.service.auth;

import pe.edu.sigestec.service.*;

public interface CuentaService {
    void cambiarClave(String username, String claveActual, String claveNueva);
}
