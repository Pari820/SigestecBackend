package pe.edu.sigestec.dto.auth;

public record LoginResponseDTO(
    String rol, String nombre, String username,
    String token, long expiraEnSegundos
) {}
