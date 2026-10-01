CREATE TABLE whatsapp_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    alert_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    recipient_phone VARCHAR(16) NOT NULL,
    alert_title VARCHAR(150) NOT NULL,
    alert_type VARCHAR(40) NOT NULL,
    alert_severity VARCHAR(20) NOT NULL,
    alert_description VARCHAR(2000) NOT NULL,
    alert_start_at TIMESTAMP(6) NOT NULL,
    alert_end_at TIMESTAMP(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    last_error VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    sent_at TIMESTAMP(6) NULL,
    CONSTRAINT pk_whatsapp_notifications PRIMARY KEY (id),
    CONSTRAINT fk_whatsapp_notifications_alert FOREIGN KEY (alert_id)
        REFERENCES climate_alerts (id) ON DELETE SET NULL,
    CONSTRAINT fk_whatsapp_notifications_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_whatsapp_notifications_alert_user UNIQUE (alert_id, user_id),
    CONSTRAINT chk_whatsapp_notifications_status CHECK (
        status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')
    ),
    CONSTRAINT chk_whatsapp_notifications_attempt_count CHECK (attempt_count >= 0),
    INDEX idx_whatsapp_notifications_status_created (status, created_at),
    INDEX idx_whatsapp_notifications_user_created (user_id, created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
