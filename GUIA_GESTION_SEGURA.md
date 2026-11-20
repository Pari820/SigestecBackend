# SIGESTEC — Ver, corregir, cancelar y eliminar sin romper el proceso

Actualización sobre la versión **SIGESTEC Backend — Recuperación SMTP**. No modifica contraseñas, perfiles de Oracle, JWT ni la recuperación por Gmail. No ejecuta sentencias `DROP TABLE` ni `TRUNCATE`.

## Criterio principal

Un registro **no se elimina** cuando otras entidades dependen de él. La acción responde con HTTP **409 Conflict** y un mensaje explicativo (`{"mensaje": "..."}`). Una solicitud todavía independiente puede eliminarse, pero las decisiones firmadas, movimientos de inventario y documentos oficiales se mantienen como historial. Las reglas también se validan en Spring Boot; no dependen únicamente de que Angular oculte un botón.

| Módulo | Ver | Editar/corregir | Eliminar / Cancelar | Reactivar |
|---|---|---|---|---|
| Solicitudes | Sí | Descripción, observaciones y lugar, solo PENDIENTE sin cotizaciones | Eliminar si no tiene cotización y está PENDIENTE; cancelar sin borrar en esas mismas condiciones | Si se canceló sin cotización |
| Cotizaciones | Sí | Formulario completo original, sin respuesta ni OC | Eliminar si está BORRADOR/ENVIADA sin OC; «Cancelar» equivale a RECHAZADA | Crear otra si corresponde; no se revierte una decisión histórica |
| Órdenes de compra | Sí | Corregir número si REGISTRADA y no tiene OT | Solo ANULAR cuando no tiene OT; nunca eliminar | Si estaba ANULADA y no tiene OT |
| Órdenes de trabajo / Asignaciones | Sí | Equipo con el formulario existente, hasta antes del ATS | CANCELAR si no tiene ATS, documentos, ejecución ni movimientos; nunca borrar la OC | Solo OT cancelada sin programación ni dependencias |
| Programaciones | Sí | Formulario existente, hasta antes del ATS | La misma cancelación de la OT, con bloqueos | Igual a la OT |
| ATS | Sí | Corregir condiciones y observaciones solo antes de aprobación y ejecución | No se elimina como registro de seguridad | No automático |
| Aprobaciones ATS | Sí | No, es una decisión histórica | No; actas y firmas se conservan | No |
| Ejecuciones | Sí | Solo narrativa y mediciones antes del informe; no se modifican consumos ni fotos | No; movimientos y evidencia deben conservarse | No |
| Informes técnicos | Sí | Texto antes de conformidad, **sin reemplazar el PDF ya emitido** | No se elimina el documento histórico | No |
| Conformidades | Sí | No, es un acta firmada | No; se conserva el acta | No |
| Clientes | Sí | Nombre, teléfono y correo | «Inactivar», bloqueado si hay solicitudes en proceso | Sí |
| Tipos de servicio | Sí | Nombre y descripción | «Inactivar», bloqueado si hay solicitudes en proceso | Sí |
| Personal | Sí | Nombre | «Inactivar», bloqueado si tiene una cuenta o asignaciones | Sí |
| Materiales | Sí | Nombre, unidad (sin historial) y stock mínimo | «Inactivar» solo cuando stock = 0. El stock real cambia mediante movimientos | Sí |
| Herramientas | Sí | Nombre y descripción | «Inactivar» solo si no hay unidades pendientes de devolución | Sí |
| Movimientos de almacén | Sí | No, se hacen ajustes compensatorios | Nunca se eliminan | No |

> **Limitación consciente:** No implementamos editar, borrar o «anular» documentos históricos firmados ni movimientos que afecten existencias. Revertirlos correctamente requiere una operación de negocio trazable con motivos, validación de autorizaciones y, cuando corresponda, una nota de crédito o rectificación, no un botón DELETE.

## Endpoints nuevos

Todos bajo `/api/v1/gestion`, autenticados con Bearer JWT y permiso por rol:

- `PUT /{modulo}/{id}` — Modificar un subconjunto **explícito** de campos.
- `PATCH /{modulo}/{id}/cancelar` — Cancelar o inactivar conservando historial; cotización pasa a RECHAZADA.
- `PATCH /{modulo}/{id}/reactivar` — Reactivar una baja segura sin duplicar registros.
- `DELETE /{modulo}/{id}` — Únicamente elimina solicitudes o cotizaciones sin vínculos. Para catálogos realiza **baja lógica**.

Los contratos originales de `/api/v1/solicitudes`, `/cotizaciones`, `/ordenes-compra`, etc. se conservan. No se altera la API de login, JWT, Gmail SMTP o recuperación de contraseña.

## Cómo probar sin tocar tu avance

1. Haz una **copia de seguridad de Oracle** antes de reemplazar el código; no uses scripts `DROP`, `TRUNCATE` o `create-drop` sobre `SIGESTEC_ADS`.
2. Abre el proyecto desde IntelliJ, mantén el perfil `oracle-dev` y tus variables actuales de Oracle, JWT y SMTP. No vuelvas a ejecutar los scripts de creación de tablas.
3. En Angular, inicia sesión como GERENTE y entra a Solicitudes. Una solicitud `PENDIENTE` sin cotización tendrá acciones para corregir, cancelar y eliminar. Usa primero una solicitud nueva **de prueba**.
4. Registra una cotización para otra solicitud de prueba; intenta eliminar la solicitud y comprueba que aparece un mensaje de bloqueo (HTTP 409). Confirma que sigue en DBeaver.
5. En cotizaciones, comprueba que no es posible eliminar una que ya tenga OC. En las órdenes, comprueba que no es posible cancelar una OC vinculada a OT.
6. Accede como SSOMA a ATS. La aprobación debe impedir la corrección posterior. Prueba también que GERENTE no puede modificar ATS.
7. Para documentos firmados, utiliza «Ver» y comprueba que el sistema explica por qué no se permite modificar o eliminar.

Consultas seguras en DBeaver:

```sql
SELECT ID_SOLICITUD, ESTADO, DESCRIPCION
  FROM SOLICITUD_SERVICIO ORDER BY ID_SOLICITUD DESC;

SELECT ID_COTIZACION, ID_SOLICITUD, ESTADO
  FROM COTIZACION ORDER BY ID_COTIZACION DESC;

SELECT ID_ORDEN_COMPRA, ID_COTIZACION, ESTADO
  FROM ORDEN_COMPRA ORDER BY ID_ORDEN_COMPRA DESC;
```

## Compilación y comprobaciones

- `mvn test`: tests existentes + `GestionSeguraTest`, con una base H2 temporal en memoria.
- `mvn spring-boot:run` o iniciar desde IntelliJ con `oracle-dev`.
- El perfil `oracle` de validación sigue disponible para una base existente si prefieres no admitir cambios automáticos de esquema.
- Las funciones de corrección se concentran en `service/GestionSeguraService.java`, y el enrutamiento en `controller/GestionSeguraController.java`. Los permisos se validan en el servidor.

**No debes activar `spring.jpa.hibernate.ddl-auto=create` ni `create-drop` sobre tu base Oracle con datos.**
