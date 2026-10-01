ALTER TABLE whatsapp_notifications
    ADD COLUMN provider_message_id VARCHAR(255) NULL AFTER last_error,
    ADD CONSTRAINT uk_whatsapp_notifications_provider_message_id UNIQUE (provider_message_id);
