CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('CITIZEN', 'ADMIN'))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE occurrences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(40) NOT NULL,
    occurrence_type VARCHAR(40) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    perceived_risk VARCHAR(20) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    neighborhood VARCHAR(100),
    address VARCHAR(255),
    image_path VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_occurrences PRIMARY KEY (id),
    CONSTRAINT fk_occurrences_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT chk_occurrences_category CHECK (
        category IN ('EVENTO_NATURAL', 'INFRAESTRUTURA_URBANA')
    ),
    CONSTRAINT chk_occurrences_category_type CHECK (
        (category = 'EVENTO_NATURAL' AND occurrence_type IN (
            'ALAGAMENTO', 'ENCHENTE', 'QUEDA_ARVORE', 'DESLIZAMENTO',
            'VENTOS_FORTES', 'RESSACA_MARITIMA', 'OUTRO'
        ))
        OR
        (category = 'INFRAESTRUTURA_URBANA' AND occurrence_type IN (
            'BURACO_RUA', 'BUEIRO_ENTUPIDO', 'POSTE_DANIFICADO',
            'SEMAFORO_COM_PROBLEMA', 'RUA_BLOQUEADA', 'FALTA_ILUMINACAO', 'OUTRO'
        ))
    ),
    CONSTRAINT chk_occurrences_risk CHECK (perceived_risk IN ('BAIXO', 'MEDIO', 'ALTO')),
    CONSTRAINT chk_occurrences_status CHECK (
        status IN ('REGISTRADA', 'EM_ANALISE', 'EM_ATENDIMENTO', 'RESOLVIDA', 'NAO_CONFIRMADA')
    ),
    CONSTRAINT chk_occurrences_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_occurrences_longitude CHECK (longitude BETWEEN -180 AND 180),
    INDEX idx_occurrences_user_created_at (user_id, created_at),
    INDEX idx_occurrences_category_type (category, occurrence_type),
    INDEX idx_occurrences_status (status),
    INDEX idx_occurrences_neighborhood (neighborhood),
    INDEX idx_occurrences_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE climate_alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    alert_type VARCHAR(40) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    start_at TIMESTAMP(6) NOT NULL,
    end_at TIMESTAMP(6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_climate_alerts PRIMARY KEY (id),
    CONSTRAINT chk_climate_alerts_type CHECK (
        alert_type IN (
            'CHUVA_INTENSA', 'ALAGAMENTO', 'VENTOS_FORTES',
            'RESSACA_MARITIMA', 'DESLIZAMENTO', 'OUTRO'
        )
    ),
    CONSTRAINT chk_climate_alerts_severity CHECK (severity IN ('BAIXA', 'MODERADA', 'ALTA')),
    CONSTRAINT chk_climate_alerts_period CHECK (end_at > start_at),
    INDEX idx_climate_alerts_visibility (active, start_at, end_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

