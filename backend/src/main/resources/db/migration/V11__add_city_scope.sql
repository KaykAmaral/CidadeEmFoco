CREATE TABLE cities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    state_code CHAR(2) NOT NULL,
    ibge_code CHAR(7) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_cities PRIMARY KEY (id),
    CONSTRAINT uk_cities_ibge_code UNIQUE (ibge_code),
    CONSTRAINT uk_cities_name_state UNIQUE (name, state_code)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO cities (name, state_code, ibge_code, active)
VALUES ('Praia Grande', 'SP', '3541000', TRUE);

ALTER TABLE users ADD COLUMN city_id BIGINT NULL;
ALTER TABLE occurrences ADD COLUMN city_id BIGINT NULL;
ALTER TABLE climate_alerts ADD COLUMN city_id BIGINT NULL;

UPDATE users SET city_id = (SELECT id FROM cities WHERE ibge_code = '3541000') WHERE city_id IS NULL;
UPDATE occurrences SET city_id = (SELECT id FROM cities WHERE ibge_code = '3541000') WHERE city_id IS NULL;
UPDATE climate_alerts SET city_id = (SELECT id FROM cities WHERE ibge_code = '3541000') WHERE city_id IS NULL;

ALTER TABLE users MODIFY COLUMN city_id BIGINT NOT NULL;
ALTER TABLE occurrences MODIFY COLUMN city_id BIGINT NOT NULL;
ALTER TABLE climate_alerts MODIFY COLUMN city_id BIGINT NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_city FOREIGN KEY (city_id) REFERENCES cities (id),
    ADD INDEX idx_users_city_role (city_id, role);

ALTER TABLE occurrences
    ADD CONSTRAINT fk_occurrences_city FOREIGN KEY (city_id) REFERENCES cities (id),
    ADD INDEX idx_occurrences_city_created_at (city_id, created_at),
    ADD INDEX idx_occurrences_city_status (city_id, status);

ALTER TABLE climate_alerts
    ADD CONSTRAINT fk_climate_alerts_city FOREIGN KEY (city_id) REFERENCES cities (id),
    ADD INDEX idx_climate_alerts_city_visibility (city_id, active, start_at, end_at);
