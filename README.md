# SIGESTEC — Backend

## Descripción

SIGESTEC es un sistema de gestión de servicios técnicos desarrollado para ACOIEL SAC. Permite administrar la información comercial, operativa y técnica de los servicios realizados por la empresa.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.7
- Spring Security y JWT
- Spring Data JPA
- Oracle Database
- Maven
- SMTP para recuperación de contraseñas

## Módulos del sistema

- Autenticación y gestión de usuarios
- Clientes y catálogos
- Solicitudes de servicio
- Cotizaciones
- Órdenes de compra
- Órdenes de trabajo
- Asignación de personal
- Programación de servicios
- Análisis de Trabajo Seguro (ATS)
- Aprobaciones de ATS
- Ejecución de servicios
- Informes técnicos
- Conformidad de servicios
- Gestión de almacén

## Arquitectura

El backend está organizado en capas:

- **Controller:** endpoints de la API REST.
- **Service:** interfaces de servicios.
- **ServiceImpl:** implementación de la lógica de negocio.
- **Repository:** acceso a los datos.
- **Entity:** entidades de persistencia.
- **DTO:** transferencia y validación de datos.
- **Config:** seguridad y configuración del sistema.

## Configuración local

El proyecto utiliza el perfil `oracle-dev` para conectarse al entorno Oracle local de desarrollo.

Las credenciales de Oracle, JWT y SMTP se configuran mediante variables de entorno.

## Ejecución

1. Iniciar Oracle Database en Docker.
2. Abrir el proyecto en IntelliJ IDEA.
3. Configurar Java 21 y las dependencias Maven.
4. Seleccionar el perfil `oracle-dev`.
5. Configurar las variables de entorno necesarias.
6. Ejecutar `SigestecApplication.java`.

## Pruebas

El repositorio incluye pruebas automatizadas y una colección Postman para verificar las operaciones de la API REST.

## Proyecto

Sistema de Gestión de Servicios Técnicos — SIGESTEC.

Desarrollado como proyecto académico de Ingeniería de Sistemas.