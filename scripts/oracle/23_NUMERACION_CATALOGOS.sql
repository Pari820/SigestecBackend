-- SIGESTEC: números públicos consecutivos para clientes, tipos de servicio y personal.
-- Ejecutar UNA VEZ desde DBeaver en SIGESTEC_ADS, con el backend detenido.
-- IMPORTANTE: realiza una copia de seguridad antes. Destinado a datos de prueba.
-- No modifica ID_PERSONA_JURIDICA, ID_TIPO_SERVICIO ni ID_EMPLEADO (claves primarias).
-- Tampoco modifica las relaciones, las órdenes, los JWT ni otros correlativos.
-- El campo NUMERO_REGISTRO es el ID PUBLICO que muestra Angular (#1, #2, ...).
-- Las tres series son independientes y no se reinician anualmente.

-- Comprobar la infraestructura de contadores existente.
DECLARE
    v_cantidad NUMBER;
    PROCEDURE agregar_columna_si_falta(p_tabla VARCHAR2) IS
        v_col NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_col FROM USER_TAB_COLUMNS
         WHERE TABLE_NAME = p_tabla AND COLUMN_NAME = 'NUMERO_REGISTRO';
        IF v_col = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || DBMS_ASSERT.SIMPLE_SQL_NAME(p_tabla) ||
                              ' ADD (NUMERO_REGISTRO NUMBER(10))';
        END IF;
    END;
BEGIN
    SELECT COUNT(*) INTO v_cantidad FROM USER_TABLES WHERE TABLE_NAME = 'CORRELATIVO_DOCUMENTO';
    IF v_cantidad = 0 THEN
        RAISE_APPLICATION_ERROR(-20023, 'Falta la tabla CORRELATIVO_DOCUMENTO: verifica la migración 21');
    END IF;
    agregar_columna_si_falta('PERSONA_JURIDICA');
    agregar_columna_si_falta('TIPO_SERVICIO');
    agregar_columna_si_falta('EMPLEADO');
END;
/

-- Utilizar valores provisionales únicos negativos para prevenir colisiones si
-- alguna columna ya tiene restricción UNIQUE en una ejecución anterior.
UPDATE PERSONA_JURIDICA SET NUMERO_REGISTRO = -ID_PERSONA_JURIDICA;
UPDATE TIPO_SERVICIO SET NUMERO_REGISTRO = -ID_TIPO_SERVICIO;
UPDATE EMPLEADO SET NUMERO_REGISTRO = -ID_EMPLEADO;

-- Orden estable: ID interno ascendente. No se cambian los IDs relacionales.
MERGE INTO PERSONA_JURIDICA destino
USING (
    SELECT ID_PERSONA_JURIDICA,
           ROW_NUMBER() OVER (ORDER BY ID_PERSONA_JURIDICA) AS NUEVO_NUMERO
    FROM PERSONA_JURIDICA
) origen
ON (destino.ID_PERSONA_JURIDICA = origen.ID_PERSONA_JURIDICA)
WHEN MATCHED THEN UPDATE SET destino.NUMERO_REGISTRO = origen.NUEVO_NUMERO;

MERGE INTO TIPO_SERVICIO destino
USING (
    SELECT ID_TIPO_SERVICIO,
           ROW_NUMBER() OVER (ORDER BY ID_TIPO_SERVICIO) AS NUEVO_NUMERO
    FROM TIPO_SERVICIO
) origen
ON (destino.ID_TIPO_SERVICIO = origen.ID_TIPO_SERVICIO)
WHEN MATCHED THEN UPDATE SET destino.NUMERO_REGISTRO = origen.NUEVO_NUMERO;

MERGE INTO EMPLEADO destino
USING (
    SELECT ID_EMPLEADO,
           ROW_NUMBER() OVER (ORDER BY ID_EMPLEADO) AS NUEVO_NUMERO
    FROM EMPLEADO
) origen
ON (destino.ID_EMPLEADO = origen.ID_EMPLEADO)
WHEN MATCHED THEN UPDATE SET destino.NUMERO_REGISTRO = origen.NUEVO_NUMERO;

-- Las siguientes restricciones se aplican solo después de completar los valores.
ALTER TABLE PERSONA_JURIDICA MODIFY (NUMERO_REGISTRO NOT NULL);
ALTER TABLE TIPO_SERVICIO MODIFY (NUMERO_REGISTRO NOT NULL);
ALTER TABLE EMPLEADO MODIFY (NUMERO_REGISTRO NOT NULL);

DECLARE
    PROCEDURE asegurar_unico(p_tabla VARCHAR2, p_restriccion VARCHAR2) IS
        v_cantidad NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_cantidad FROM USER_CONSTRAINTS
        WHERE TABLE_NAME = p_tabla AND CONSTRAINT_NAME = p_restriccion;
        IF v_cantidad = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || DBMS_ASSERT.SIMPLE_SQL_NAME(p_tabla) ||
                              ' ADD CONSTRAINT ' || DBMS_ASSERT.SIMPLE_SQL_NAME(p_restriccion) ||
                              ' UNIQUE (NUMERO_REGISTRO)';
        END IF;
    END;
BEGIN
    asegurar_unico('PERSONA_JURIDICA', 'UQ_SIGESTEC_CLIENTE_NUMERO');
    asegurar_unico('TIPO_SERVICIO', 'UQ_SIGESTEC_TIPO_NUMERO');
    asegurar_unico('EMPLEADO', 'UQ_SIGESTEC_PERSONAL_NUMERO');
END;
/

-- Sincronizar contadores transaccionales con el máximo de cada catálogo.
MERGE INTO CORRELATIVO_DOCUMENTO destino
USING (
    SELECT 'CLIENTE' AS TIPO_DOCUMENTO, 0 AS ANIO,
           NVL(MAX(NUMERO_REGISTRO), 0) AS ULTIMO_NUMERO FROM PERSONA_JURIDICA
    UNION ALL
    SELECT 'TIPO_SERVICIO', 0, NVL(MAX(NUMERO_REGISTRO), 0) FROM TIPO_SERVICIO
    UNION ALL
    SELECT 'PERSONAL', 0, NVL(MAX(NUMERO_REGISTRO), 0) FROM EMPLEADO
) origen
ON (destino.TIPO_DOCUMENTO = origen.TIPO_DOCUMENTO AND destino.ANIO = origen.ANIO)
WHEN MATCHED THEN UPDATE SET destino.ULTIMO_NUMERO =
    GREATEST(destino.ULTIMO_NUMERO, origen.ULTIMO_NUMERO)
WHEN NOT MATCHED THEN INSERT (TIPO_DOCUMENTO, ANIO, ULTIMO_NUMERO)
VALUES (origen.TIPO_DOCUMENTO, origen.ANIO, origen.ULTIMO_NUMERO);

COMMIT;

-- Verificación: 1, 2, 3 ... N en cada catálogo; IDs internos permanecen intactos.
SELECT NUMERO_REGISTRO, ID_PERSONA_JURIDICA, RAZON_SOCIAL, RUC
  FROM PERSONA_JURIDICA ORDER BY NUMERO_REGISTRO;
SELECT NUMERO_REGISTRO, ID_TIPO_SERVICIO, NOMBRE
  FROM TIPO_SERVICIO ORDER BY NUMERO_REGISTRO;
SELECT NUMERO_REGISTRO, ID_EMPLEADO, CODIGO_EMPLEADO
  FROM EMPLEADO ORDER BY NUMERO_REGISTRO;
SELECT TIPO_DOCUMENTO, ANIO, ULTIMO_NUMERO
  FROM CORRELATIVO_DOCUMENTO
 WHERE TIPO_DOCUMENTO IN ('CLIENTE', 'TIPO_SERVICIO', 'PERSONAL')
 ORDER BY TIPO_DOCUMENTO;
