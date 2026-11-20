# SIGESTEC Backend — Oracle + DBeaver + Docker + IntelliJ

> La base de datos es **Oracle**. **DBeaver es su administrador**; no es un servidor de BD.
> El backend se conecta por JDBC al mismo Oracle que consultas desde DBeaver.

## Estado del proyecto y decisión de estructura

Este backend **ya sigue** el orden enseñado en PharmaBackend:
`controller/`, `dto/`, `entity/`, `repository/`, `service/`, `service/impl/`,
`config/`, `exception/` y `storage/`.
No se movió ningún controlador, entidad, servicio ni endpoint: reorganizarlos no
agregaría funcionalidad y elevaría el riesgo de romper los imports.

## Dos formas de usar Oracle

| Perfil | Para qué sirve | `ddl-auto` | ¿Crea tablas al iniciar? |
| --- | --- | --- | --- |
| `oracle-dev` (nuevo) | Oracle personal **de pruebas**, esquema limpio, primera instalación | `update` | **Sí**, crea las tablas faltantes que corresponden a entidades JPA |
| `oracle` (original) | BD ADS ya creada / datos valiosos | `validate` | **No**: solo verifica |
| ninguno (original) | Pruebas H2 locales | `update` | Sí, en H2 (no Oracle) |

`update` se ejecuta **cada vez** que arrancas, normalmente sin borrar tablas;
**no es una migración segura**: puede agregar columnas, no gestiona todas las
restricciones, índices y cambios de datos, y puede fallar ante conflictos.
Se recomienda **solo** para un Oracle de prácticas aislado. Nunca uses `create`
o `create-drop` en una base con información que necesites conservar.

## Opción A: ya tienes Oracle en Docker / DBeaver

1. En Docker Desktop comprueba que el contenedor Oracle esté **running** y que
   el puerto del equipo (por ejemplo 1521) esté publicado.
2. En DBeaver -> Nueva conexión -> Oracle:
   - Host `localhost`, Puerto `1521` (o el publicado), tipo **Service Name**.
   - Service Name `FREEPDB1` (verifica que ese sea tu servicio real).
   - Usuario `SIGESTEC_ADS`, contraseña **la de ese mismo usuario**.
   - Pulsa **Test Connection** y luego **Finish**.
3. Si no tienes ese usuario Oracle, solicítalo al administrador o usa
   `scripts/oracle/00_CREAR_USUARIO_OPCIONAL.sql` desde **SYSTEM conectado a la PDB**.
   Si ya existe, no vuelvas a crearlo.
4. Ejecuta `scripts/oracle/00_COMPROBAR_CONEXION_DBEAVER.sql` para ver el esquema
   actual. Si tiene datos importantes o ya contiene las tablas del ADS, utiliza
   perfil **`oracle`**, NO `oracle-dev`.

## Opción B: crear un Oracle nuevo para SIGESTEC con Docker

Solo si **no** tienes ya un Oracle que usar. Requiere Docker Desktop funcionando:

```powershell
Copy-Item .env.oracle.example .env.oracle.local
# Editar .env.oracle.local y reemplazar ORACLE_SYS_PASSWORD / ORACLE_PASSWORD.
docker compose --env-file .env.oracle.local -f docker-compose.oracle.yml up -d
docker compose --env-file .env.oracle.local -f docker-compose.oracle.yml ps
```

El contenedor crea la base `FREEPDB1` y el usuario aplicativo `SIGESTEC_ADS`.
Sus datos permanecen en el volumen Docker `sigestec-oracle-data`.
**No ejecutes** los scripts `01_TABLAS...` y `04_EXTENSION...` si vas a usar la
creación automática de Hibernate sobre ese mismo esquema.

Si 1521 está ocupado, modifica `ORACLE_PORT=1523` en `.env.oracle.local`,
usa puerto **1523** en DBeaver y `ORACLE_URL=jdbc:oracle:thin:@//localhost:1523/FREEPDB1`
en IntelliJ. El contenedor puede requerir un tiempo de inicialización; verifica
su estado `healthy` antes de conectarte.

## IntelliJ IDEA — conexión y generación de tablas

1. **File > Open** y elige la carpeta `sigestec-backend` (contiene `pom.xml`).
2. Espera a que Maven importe las dependencias; configura **JDK 21**.
3. Abre `SigestecApplication.java` y crea una configuración de ejecución
   **Spring Boot** (o desde **Run > Edit Configurations**).
4. En **Active profiles** escribe exactamente `oracle-dev` para una BD nueva
   **solo de prácticas**, o `oracle` si ya existe el modelo ADS.
5. En **Environment variables** agrega:
   - `ORACLE_URL=jdbc:oracle:thin:@//localhost:1521/FREEPDB1`
   - `ORACLE_USER=SIGESTEC_ADS`
   - `ORACLE_PASSWORD=...` (contraseña real del usuario Oracle)
   - `JWT_SECRET_BASE64=...` (clave base64 aleatoria de 48 bytes o más)
   - `SIGESTEC_DEMO=false` (recomendado inicialmente)
6. Presiona **Run** en `SigestecApplication` y consulta el registro de arranque.
   Hibernate intenta crear las tablas de las **36 entidades JPA** con `oracle-dev`.
   En DBeaver, **Refresh** sobre `SIGESTEC_ADS > Tables` para verlas.
7. No copies la contraseña en `application.properties`, código, Git ni capturas.
   No es suficiente rellenar `.env.oracle.local` para IntelliJ: **no la lee
   automáticamente**. Debes definir las variables en Run/Debug Configurations.

Para generar el valor `JWT_SECRET_BASE64` en PowerShell:

```powershell
$bytes = New-Object byte[] 48
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Alternativa con terminal en la que **ya definiste las variables de entorno**:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=oracle-dev"
```

## ¿Se crean también los usuarios de login?

No por defecto. Crear tablas **no significa insertar usuarios ni contraseñas**.
El perfil `oracle-dev` establece `SIGESTEC_DEMO=false` por seguridad.
Si acabas de crear un esquema Oracle completamente **vacío y de prácticas**,
opcionalmente puedes configurar `SIGESTEC_DEMO=true` y definir una clave distinta
en `SIGESTEC_DEMO_PASSWORD`. Esto activa el sembrado de datos incluido en el backend,
que está pensado para demostración; **NO** lo actives en el esquema ADS real.
El sembrado en Oracle no ha sido probado contra un contenedor real aquí.

## ¿Qué pasa con los scripts del esquema ADS?

El ZIP original contiene SQL manual para 26 tablas iniciales y 10 de ampliación,
que corresponden a 36 entidades JPA. El modelo ADS completo mencionado en los
README originales incluye **45 tablas**; Hibernate solo conoce las **36 entidades**
del proyecto. Por tanto, no debes considerar `oracle-dev` equivalente a la
creación del modelo ADS completo.

Para una base Oracle ya existente, usa `oracle` con `ddl-auto=validate` y revisa
`scripts/oracle/02_VALIDAR_TABLAS_ADS.sql` y
`scripts/oracle/05_VALIDAR_CU07_CU12.sql`. Si te faltan tablas, concilia primero
el modelo, la versión y los scripts. No combines scripts y actualización automática
sin verificar si el esquema ya tiene objetos creados.

Para entregas o producción, la alternativa recomendada es utilizar **migraciones
versionadas** (por ejemplo, Flyway) y `ddl-auto=validate` una vez validada su
compatibilidad con el esquema real.

## Problemas frecuentes

- `ORA-01017`: usuario o clave incorrecta.
- `ORA-12514`: service name o puerto incorrectos, o Oracle aún iniciando.
- `ORA-01031` o `ORA-01950`: faltan privilegios/cuota para crear objetos.
- `Schema-validation: missing table`: perfil `oracle` con tablas faltantes;
  no significa que debas cambiar a `update` sobre la base real.
- Backend iniciado usando H2: no seleccionaste un perfil Oracle en IntelliJ.
- DBeaver no muestra nuevas tablas: refresca el esquema y comprueba `SELECT USER FROM dual`.
- El frontend en `http://localhost:4200` consume la API en `http://localhost:8080/api/v1`.
