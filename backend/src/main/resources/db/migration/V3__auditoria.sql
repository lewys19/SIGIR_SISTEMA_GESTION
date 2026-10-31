CREATE TABLE auditoria (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actor      VARCHAR(160) NOT NULL,
    accion     VARCHAR(40)  NOT NULL,
    entidad    VARCHAR(40),
    entidad_id VARCHAR(40),
    detalle    TEXT
);
CREATE INDEX idx_auditoria_fecha  ON auditoria (fecha DESC);
CREATE INDEX idx_auditoria_accion ON auditoria (accion);

-- Bitácora inmutable: ni la propia aplicación puede modificar o borrar registros
CREATE FUNCTION auditoria_inmutable() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'La auditoría es de solo inserción';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria_inmutable
    BEFORE UPDATE OR DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION auditoria_inmutable();
