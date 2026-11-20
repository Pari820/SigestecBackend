-- SOLO si usas Oracle EXISTENTE (no el docker-compose que YA CREA APP_USER).
-- Conectarse en DBeaver como SYSTEM a FREEPDB1; no a CDB$ROOT.
-- No ejecutar en una BD compartida sin autorizacion del administrador.
-- Cambia la clave y revisa tablespace/cuota con el administrador Oracle.
-- Si SIGESTEC_ADS ya existe, NO ejecutar este archivo.
CREATE USER SIGESTEC_ADS IDENTIFIED BY "CAMBIAR_CLAVE_SEGURA"
  DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW TO SIGESTEC_ADS;
ALTER USER SIGESTEC_ADS QUOTA 200M ON USERS;
