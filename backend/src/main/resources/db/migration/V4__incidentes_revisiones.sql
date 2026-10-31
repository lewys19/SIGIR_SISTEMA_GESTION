CREATE SEQUENCE incidente_codigo_seq;

CREATE TABLE incidentes (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo               VARCHAR(20)  NOT NULL UNIQUE,
    titulo               VARCHAR(200) NOT NULL,
    descripcion          TEXT         NOT NULL,
    tipo                 VARCHAR(30)  NOT NULL,
    severidad            VARCHAR(10)  NOT NULL CHECK (severidad IN ('BAJA','MEDIA','ALTA','CRITICA')),
    estado               VARCHAR(15)  NOT NULL CHECK (estado IN ('BORRADOR','EN_REVISION','APROBADO','RECHAZADO','CERRADO')),
    regulador            VARCHAR(20)  NOT NULL,
    area                 VARCHAR(80),
    fecha_ocurrencia     DATE         NOT NULL,
    fecha_deteccion      DATE         NOT NULL,
    fecha_limite_reporte DATE         NOT NULL,
    impacto              TEXT,
    medidas              TEXT,
    reportado_por_id     BIGINT       NOT NULL REFERENCES usuarios (id),
    creado_en            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    actualizado_en       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    version              BIGINT       NOT NULL DEFAULT 0,
    CHECK (fecha_deteccion >= fecha_ocurrencia)
);
CREATE INDEX idx_incidentes_estado    ON incidentes (estado);
CREATE INDEX idx_incidentes_severidad ON incidentes (severidad);
CREATE INDEX idx_incidentes_creado    ON incidentes (creado_en);

CREATE TABLE revisiones (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    incidente_id BIGINT      NOT NULL REFERENCES incidentes (id),
    revisor_id   BIGINT      NOT NULL REFERENCES usuarios (id),
    decision     VARCHAR(15) NOT NULL CHECK (decision IN ('ENVIADO','APROBADO','RECHAZADO','CERRADO')),
    comentario   TEXT,
    fecha        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_revisiones_incidente ON revisiones (incidente_id);
