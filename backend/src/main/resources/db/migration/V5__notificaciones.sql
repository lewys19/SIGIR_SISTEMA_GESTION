CREATE TABLE notificaciones (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id   BIGINT       NOT NULL REFERENCES usuarios (id),
    tipo         VARCHAR(30)  NOT NULL,
    titulo       VARCHAR(200) NOT NULL,
    mensaje      TEXT,
    incidente_id BIGINT REFERENCES incidentes (id),
    leida        BOOLEAN      NOT NULL DEFAULT FALSE,
    creada_en    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_notificaciones_usuario ON notificaciones (usuario_id, leida, creada_en DESC);
