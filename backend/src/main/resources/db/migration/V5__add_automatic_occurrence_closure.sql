ALTER TABLE occurrences
    ADD COLUMN automatically_resolved BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE occurrence_auto_closures (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    occurrence_id BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    closed_at TIMESTAMP(6) NOT NULL,
    validity_seconds BIGINT NOT NULL,
    CONSTRAINT uk_auto_closure_occurrence UNIQUE (occurrence_id),
    CONSTRAINT fk_auto_closure_occurrence FOREIGN KEY (occurrence_id) REFERENCES occurrences (id)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

ALTER TABLE occurrences DROP CHECK chk_occurrences_category_type;
ALTER TABLE occurrences ADD CONSTRAINT chk_occurrences_category_type CHECK (
    (category = 'EVENTO_NATURAL' AND occurrence_type IN (
        'ALAGAMENTO', 'ENCHENTE', 'QUEDA_ARVORE', 'DESLIZAMENTO', 'VENTOS_FORTES',
        'RESSACA_MARITIMA', 'INCENDIO', 'CHUVA_INTENSA', 'QUEDA_GRANIZO', 'OUTRO'
    )) OR (category = 'INFRAESTRUTURA_URBANA' AND occurrence_type IN (
        'BURACO_RUA', 'BUEIRO_ENTUPIDO', 'POSTE_DANIFICADO', 'SEMAFORO_COM_PROBLEMA',
        'RUA_BLOQUEADA', 'FALTA_ILUMINACAO', 'ARVORE_OBSTRUINDO_VIA', 'OUTRO'
    ))
);

CREATE INDEX idx_occurrences_auto_closure ON occurrences (category, status, occurrence_type, created_at);
