# SIGESTEC — Recuperación de contraseña con SMTP + Spring Boot

**Basado en:** última versión SIGESTEC Backend con recuperación Brevo.

## Qué cambió

- El servicio `CorreoTransaccionalService` usa `JavaMailSender` y `SimpleMailMessage`, del starter `spring-boot-starter-mail`.
- Se eliminó el uso de la API de Brevo: ya no son necesarias `BREVO_API_KEY` ni `BREVO_REMITENTE_EMAIL`.
- Se conservan los endpoints `/api/v1/auth/recuperar-clave` y `/api/v1/auth/restablecer-clave`, así como el frontend sin cambios en sus peticiones.
- La tabla `RECUPERACION_CLAVE` guarda solo hash SHA-256, el enlace vence en 15 minutos, es de un solo uso y los JWT viejos se invalidan tras restablecer la clave.
- Se mantienen el temporizador JWT, los roles, la cuenta `admin`, el cambio de clave dentro de la sesión y las 36 tablas funcionales del backend.

## Gmail: habilitar contraseña de aplicación

1. En la cuenta **sigestec835@gmail.com**, activa la **verificación en dos pasos** de Google.
2. Abre https://myaccount.google.com/apppasswords con esa cuenta e intenta crear una contraseña de aplicación llamada **SIGESTEC Spring Boot**. Algunas cuentas no permiten esta función según su configuración de seguridad.
3. Guarda el valor de 16 caracteres **en IntelliJ > Run > Edit Configurations > SigestecApplication > Environment variables**. No uses tu contraseña normal de Gmail.
4. No pegues esa contraseña en Java, `application.properties`, GitHub ni capturas que compartas.

Google: https://support.google.com/accounts/answer/185833?hl=es-419
Spring Boot: https://docs.spring.io/spring-boot/reference/io/email.html

## Variables en IntelliJ

**Active profiles:** `oracle-dev` (solo para esquema Oracle local de prácticas).

```text
ORACLE_URL=jdbc:oracle:thin:@//localhost:1522/FREEPDB1
ORACLE_USER=SIGESTEC_ADS
ORACLE_PASSWORD=TU_CONTRASENA_DE_ORACLE
JWT_SECRET_BASE64=TU_SECRETO_ACTUAL
JWT_EXPIRACION_MINUTOS=120
SIGESTEC_DEMO=false
SIGESTEC_RECUPERACION_ENABLED=true
SIGESTEC_RECUPERACION_CORREO_PRUEBA=sigestec835@gmail.com
SIGESTEC_FRONTEND_URL=http://localhost:4200
SMTP_USERNAME=sigestec835@gmail.com
SMTP_PASSWORD=TU_CONTRASENA_DE_APLICACION_GMAIL
SMTP_REMITENTE_EMAIL=sigestec835@gmail.com
```

Variables opcionales (las primeras dos ya tienen estos valores predeterminados):

```text
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_REMITENTE_NOMBRE=SIGESTEC
```

**Importante:** `SMTP_PASSWORD` es la contraseña de aplicación Gmail. `ORACLE_PASSWORD` es la contraseña del esquema Oracle; `JWT_SECRET_BASE64` es otra clave distinta. **No cambies ni reveles la clave JWT existente** sin necesidad.

Puerto SMTP: 587 con STARTTLS obligatorio. Los tiempos de espera están configurados en `application.properties`. Si utilizas otro servidor SMTP, modifica host, puerto y sus requisitos de autenticación/TLS.

## Prueba paso a paso

1. Mantén Docker `oracle-local` ejecutándose; conserva el usuario Oracle `SIGESTEC_ADS` y sus tablas.
2. Inicia el backend en IntelliJ (perfil `oracle-dev`) y comprueba que no haya error de inicialización. Maven descargará `spring-boot-starter-mail` si todavía no lo tienes en tu caché.
3. Ejecuta Angular en VS Code con `npm start`; abre http://localhost:4200/login.
4. Pulsa **¿Olvidaste tu contraseña?**, introduce `ssoma` o `gerente` y envía la solicitud.
5. Revisa **sigestec835@gmail.com** (también Spam): debe llegar un enlace que dirige a `http://localhost:4200/restablecer-clave?token=...`. Si enviás y recibís en la misma cuenta Gmail, revisa Recibidos/Todos.
6. Abre el enlace **en la misma computadora** donde corre Angular, ingresa una nueva contraseña diferente a la anterior y confirma.
7. Vuelve a iniciar sesión. Comprueba que el enlace anterior ya no se puede utilizar y que los JWT antiguos quedan inválidos.
8. Si la prueba falla, revisa **los logs de IntelliJ sin copiar las credenciales**. Errores `535` suelen significar autenticación SMTP rechazada; `Connection timed out` suele indicar conectividad o bloqueo de puertos.

Consultas útiles en DBeaver:

```sql
SELECT COUNT(*) AS TOTAL_RECUPERACIONES FROM RECUPERACION_CLAVE;
SELECT NOMBRE_USUARIO, TOKEN_VERSION FROM USUARIO ORDER BY NOMBRE_USUARIO;
```

No ejecutes nuevamente scripts de creación de tablas si ya existen. `oracle-dev` usa `ddl-auto=update` únicamente para tu base de prácticas.

## Seguridad

- **Solo para desarrollo local:** `oracle-dev` redirige la recuperación de **todas** las cuentas demo (incluido `admin`) a `sigestec835@gmail.com`; cualquier persona con acceso a ese Gmail puede recuperar esas cuentas. No publicarlo así.
- En un entorno real, desactiva `app.recuperacion.test-mode`, asigna un correo verificado distinto a cada cuenta, activa HTTPS y un limitador de solicitudes persistente.
- Las respuestas de recuperación no confirman si existe el usuario. Los enlaces usan tokens temporales; no son JWT.
- Es recomendable utilizar un buzón separado solo para pruebas. Si se filtra la contraseña de aplicación, revócala desde Google.
- Evita reenviar el enlace de recuperación o publicarlo en capturas.
