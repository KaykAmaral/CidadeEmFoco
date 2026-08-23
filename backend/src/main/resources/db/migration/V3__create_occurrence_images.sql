CREATE TABLE occurrence_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    occurrence_id BIGINT NOT NULL,
    image_path VARCHAR(500) NOT NULL,
    CONSTRAINT pk_occurrence_images PRIMARY KEY (id),
    CONSTRAINT fk_occurrence_images_occurrence FOREIGN KEY (occurrence_id)
        REFERENCES occurrences (id) ON DELETE CASCADE,
    INDEX idx_occurrence_images_occurrence (occurrence_id)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO occurrence_images (occurrence_id, image_path)
SELECT id, image_path FROM occurrences WHERE image_path IS NOT NULL;
