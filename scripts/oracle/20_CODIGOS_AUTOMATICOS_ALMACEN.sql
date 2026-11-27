-- NOTA: estas secuencias pertenecen a una version anterior. La version actual usa
-- CORRELATIVO_DOCUMENTO (ver 21_NUMERACION_CORRELATIVA_DOCUMENTOS.sql).
-- No es necesario volver a ejecutar este script; las secuencias existentes quedan sin uso.
-- SIGESTEC: ejecutar UNA VEZ en el esquema SIGESTEC_ADS antes de iniciar backend.
-- Se crean dos secuencias sin alterar registros preexistentes.
DECLARE
  v_inicio NUMBER;
  v_existe NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_existe FROM user_sequences WHERE sequence_name = 'SIGESTEC_SEQ_CODIGO_MATERIAL';
  IF v_existe = 0 THEN
    SELECT NVL(MAX(TO_NUMBER(SUBSTR(codigo, 5))), 0) + 1 INTO v_inicio
    FROM MATERIAL WHERE REGEXP_LIKE(codigo, '^MAT-[0-9]{1,20}$');
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SIGESTEC_SEQ_CODIGO_MATERIAL START WITH ' || v_inicio || ' INCREMENT BY 1 NOCACHE NOCYCLE';
  END IF;
  SELECT COUNT(*) INTO v_existe FROM user_sequences WHERE sequence_name = 'SIGESTEC_SEQ_CODIGO_HERRAMIENTA';
  IF v_existe = 0 THEN
    SELECT NVL(MAX(TO_NUMBER(SUBSTR(codigo_herramienta, 5))), 0) + 1 INTO v_inicio
    FROM HERRAMIENTA_EQUIPO WHERE REGEXP_LIKE(codigo_herramienta, '^HER-[0-9]{1,20}$');
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SIGESTEC_SEQ_CODIGO_HERRAMIENTA START WITH ' || v_inicio || ' INCREMENT BY 1 NOCACHE NOCYCLE';
  END IF;
END;
/
-- Comprobación (no avanzar secuencias):
SELECT sequence_name, last_number FROM user_sequences
WHERE sequence_name IN ('SIGESTEC_SEQ_CODIGO_MATERIAL','SIGESTEC_SEQ_CODIGO_HERRAMIENTA');
