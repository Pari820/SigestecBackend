package pe.edu.sigestec.service.service.auth;

import pe.edu.sigestec.dto.auth.*;
import pe.edu.sigestec.service.*;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO datos);
    CuentaResponseDTO miCuenta(String username);
}
