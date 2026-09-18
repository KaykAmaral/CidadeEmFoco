ALTER TABLE occurrences
    ADD COLUMN resolved_at TIMESTAMP(6) NULL AFTER updated_at;

UPDATE occurrences
SET resolved_at = updated_at
WHERE status = 'RESOLVIDA';

CREATE INDEX idx_occurrences_map_visibility
    ON occurrences (status, resolved_at);
