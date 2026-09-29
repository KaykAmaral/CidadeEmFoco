ALTER TABLE occurrences ADD COLUMN resolved_at TIMESTAMP(6) NULL;

-- Legacy rows have no resolution history; updated_at is the best available estimate.
UPDATE occurrences SET resolved_at = updated_at, updated_at = updated_at WHERE status = 'RESOLVIDA';

CREATE INDEX idx_occurrences_status_resolved_at ON occurrences (status, resolved_at);
