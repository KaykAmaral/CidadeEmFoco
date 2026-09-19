ALTER TABLE occurrences
    ADD COLUMN group_root_id BIGINT NULL AFTER user_id,
    ADD COLUMN strength INT NOT NULL DEFAULT 1 AFTER group_root_id,
    ADD CONSTRAINT fk_occurrences_group_root
        FOREIGN KEY (group_root_id) REFERENCES occurrences (id),
    ADD CONSTRAINT chk_occurrences_strength CHECK (strength >= 1);

CREATE INDEX idx_occurrences_group_candidates
    ON occurrences (group_root_id, category, occurrence_type, status, created_at);
