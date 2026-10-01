ALTER TABLE users
    ADD COLUMN whatsapp_phone VARCHAR(16) NULL AFTER role,
    ADD COLUMN whatsapp_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE AFTER whatsapp_phone,
    ADD COLUMN whatsapp_consent_at TIMESTAMP(6) NULL AFTER whatsapp_notifications_enabled,
    ADD CONSTRAINT uk_users_whatsapp_phone UNIQUE (whatsapp_phone),
    ADD CONSTRAINT chk_users_whatsapp_preferences CHECK (
        (whatsapp_notifications_enabled = FALSE AND whatsapp_phone IS NULL AND whatsapp_consent_at IS NULL)
        OR
        (whatsapp_notifications_enabled = TRUE AND whatsapp_phone IS NOT NULL AND whatsapp_consent_at IS NOT NULL)
    );
