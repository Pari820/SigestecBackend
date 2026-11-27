package pe.edu.sigestec.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.sigestec.entity.auth.Usuario;
import pe.edu.sigestec.repository.auth.UsuarioRepository;
import pe.edu.sigestec.repository.auth.UsuarioRolRepository;
import pe.edu.sigestec.security.TokenService;
import pe.edu.sigestec.dto.common.ErrorResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(UsuarioRepository repo, UsuarioRolRepository roles) {
        return username -> {
            Usuario u = repo.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

            if (!"A".equals(u.getEstado())) {
                throw new UsernameNotFoundException("Credenciales inválidas");
            }
            List<SimpleGrantedAuthority> a = roles.findByUsuarioId(u.getId()).stream()
                    .filter(ur -> "A".equals(ur.getRol().getEstado()))
                    .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRol().getNombre())).toList();
            return User.withUsername(u.getNombreUsuario()).password(u.getClaveHash()).authorities(a).build();
        };
    }

    @Bean
    public AuthenticationManager manager(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(users);
        p.setPasswordEncoder(encoder);
        return new ProviderManager(p);
    }

    /**
     * JwtFilter debe ejecutarse exclusivamente dentro de la cadena de Spring Security.
     * Al ser un Filter bean, Spring Boot también podría registrarlo como filtro del
     * contenedor Servlet; se deshabilita ese registro automático para evitar doble
     * ejecución y mantener un único punto de autenticación.
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterServletRegistration(JwtFilter jwt) {
        FilterRegistrationBean<JwtFilter> registration = new FilterRegistrationBean<>(jwt);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    public SecurityFilterChain security(
            HttpSecurity http, JwtFilter jwt, CorsConfigurationSource corsConfigurationSource,
            ObjectMapper json
    ) throws Exception {
        return http.csrf(x -> x.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(x -> x
                        .authenticationEntryPoint((req, res, e) -> writeSecurityError(
                                json, req, res, 401, "Autenticación requerida"))
                        .accessDeniedHandler((req, res, e) -> writeSecurityError(
                                json, req, res, 403, "No tiene permisos para realizar esta operación")))
                .authorizeHttpRequests(x -> x
                        // Un error lanzado durante el POST puede generar un ERROR dispatch a /error.
                        // Ese despacho debe llegar al manejador real; no debe convertirse en un 401
                        // que oculte la excepción original ni provocar el cierre de sesión en Angular.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/recuperar-clave",
                                "/api/v1/auth/restablecer-clave").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }

    /** Respuesta uniforme para errores de los filtros, antes de llegar a los Controllers. */
    private static void writeSecurityError(ObjectMapper json, HttpServletRequest request,
                                           HttpServletResponse response, int status, String mensaje)
            throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        json.writeValue(response.getWriter(), new ErrorResponseDTO(
                Instant.now().toString(), status,
                status == 401 ? "Unauthorized" : "Forbidden",
                mensaje, request.getRequestURI(), null, null));
    }

    @Component
    public static class JwtFilter extends OncePerRequestFilter {
        private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

        private final TokenService tokens;
        private final UserDetailsService users;
        private final UsuarioRepository usuarios;

        public JwtFilter(TokenService tokens, UserDetailsService users, UsuarioRepository usuarios) {
            this.tokens = tokens;
            this.users = users;
            this.usuarios = usuarios;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws java.io.IOException, ServletException {
            String h = req.getHeader("Authorization");
            if (h != null && h.startsWith("Bearer ")) {
                try {
                    var claims = tokens.validar(h.substring(7));
                    String username = claims.getSubject();
                    Usuario account = usuarios.findByNombreUsuario(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
                    Object rawVersion = claims.get("sv");
                    if (!(rawVersion instanceof Number) ||
                            ((Number) rawVersion).longValue() != account.getTokenVersion()) {
                        throw new org.springframework.security.authentication.BadCredentialsException("Sesión invalidada");
                    }
                    UserDetails u = users.loadUserByUsername(username);
                    var a = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
                    org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(a);
                } catch (JwtException | org.springframework.security.core.AuthenticationException e) {
                    // No registrar nunca el token. El motivo sí permite diagnosticar
                    // firma inválida, expiración, sesión revocada o cuenta inactiva.
                    log.warn("JWT rechazado para {} {}: {} ({})",
                            req.getMethod(), req.getRequestURI(), e.getMessage(),
                            e.getClass().getSimpleName());
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                }
            }
            chain.doFilter(req, res);
        }
    }
}
