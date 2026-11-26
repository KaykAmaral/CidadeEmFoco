CREATE TABLE citizen_xp_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    occurrence_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    points INT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_citizen_xp_events PRIMARY KEY (id),
    CONSTRAINT uk_citizen_xp_event UNIQUE (user_id, occurrence_id, event_type),
    CONSTRAINT fk_citizen_xp_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_citizen_xp_occurrence FOREIGN KEY (occurrence_id) REFERENCES occurrences (id),
    CONSTRAINT chk_citizen_xp_points CHECK (points > 0),
    INDEX idx_citizen_xp_city_period (created_at, user_id),
    INDEX idx_citizen_xp_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO citizen_xp_events (user_id, occurrence_id, event_type, points, created_at)
SELECT user_id, id, 'REPORT_CREATED', 10, created_at
FROM occurrences;

INSERT INTO citizen_xp_events (user_id, occurrence_id, event_type, points, created_at)
SELECT report.user_id,
       report.id,
       'CASE_RESOLVED',
       20,
       COALESCE(root_case.resolved_at, root_case.updated_at)
FROM occurrences report
JOIN occurrences root_case
  ON root_case.id = COALESCE(report.group_root_id, report.id)
WHERE root_case.status = 'RESOLVIDA';
