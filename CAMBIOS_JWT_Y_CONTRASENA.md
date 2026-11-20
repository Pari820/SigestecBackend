# SIGESTEC: JWT configurable y cambio de contraseña

## Configuración (backend)

La duración se configura en `src/main/resources/application.properties`:

```properties
app.jwt.expiracion-minutos=${JWT_EXPIRACION_MINUTOS:120}
```

- Valor predeterminado: 120 minutos (igual que antes).
- Ejemplo: 60 para una hora; 15 para 15 minutos.
- En IntelliJ / `oracle-dev` se puede añadir `JWT_EXPIRACION_MINUTOS=60` a las variables de entorno sin tocar Java.
- No modifica `ORACLE_URL`, `ORACLE_USER`, `ORACLE_PASSWORD`, `SIGESTEC_DEMO`, ni los esquemas.
- `TokenService` configura la caducidad real y `AuthController` devuelve el mismo tiempo al frontend.
- El endpoint autenticado `POST /api/v1/auth/cambiar-clave` recibe `claveActual` y `claveNueva`.
- La clave nueva se guarda con `PasswordEncoder` (BCrypt) en `USUARIO.CLAVE_HASH`.
- No debe ejecutarse `CREATE TABLE` ni reiniciar el esquema para esto.

## Cómo probar

1. Conserva Docker y DBeaver como hasta ahora; no recrees tablas.
2. En IntelliJ abre este backend (JDK 21 recomendado) y mantén el perfil `oracle-dev`.
3. Conserva tus variables locales de Oracle. Este ZIP se entrega sin `.idea/workspace.xml` por seguridad.
4. Opcional: añade `JWT_EXPIRACION_MINUTOS=60` en Environment variables.
5. Inicia Spring Boot y después el frontend Angular con `npm install` y `npm start`.
6. Inicia sesión como usuario de demostración; comprueba el temporizador en el encabezado.
7. En el menú izquierdo abre **Cambiar contraseña**. Escribe la clave actual y una nueva (8 caracteres como mínimo).
8. Se cerrará la sesión. Vuelve a ingresar con la contraseña nueva.
9. Comprueba con DBeaver que el hash cambia, sin mostrar contraseñas en texto claro:

```sql
SELECT NOMBRE_USUARIO, ESTADO, LENGTH(CLAVE_HASH) AS LONGITUD_HASH
FROM USUARIO
ORDER BY NOMBRE_USUARIO;
```

## Seguridad y límites actuales

- «¿Olvidaste tu contraseña?» NO permite restablecimiento anónimo; sigue mostrando el aviso de contactar al administrador. Se necesitaría identidad verificada (correo o procedimiento administrativo autorizado).
- El cambio de contraseña exige autenticación y la contraseña anterior. Solo modifica el usuario autenticado.
- Con JWT sin estado, **los tokens ya emitidos siguen siendo válidos hasta su expiración**, incluso después de cambiar la clave. El frontend cierra la sesión actual al actualizarla. Revocar tokens antiguos requeriría una estrategia adicional.
- `SIGESTEC_DEMO=true` es exclusivamente para desarrollo. Desactívalo tras completar pruebas; cambiar la contraseña de un usuario demo no modifica la variable de inicialización.
- Si no defines `JWT_SECRET_BASE64`, la clave JWT se genera al inicio del backend y los tokens previos no sobrevivirán a reinicios.
