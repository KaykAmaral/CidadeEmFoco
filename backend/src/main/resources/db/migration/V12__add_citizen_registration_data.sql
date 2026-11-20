ALTER TABLE users
    ADD COLUMN cpf VARCHAR(11) NULL AFTER email,
    ADD COLUMN street VARCHAR(150) NULL AFTER whatsapp_consent_at,
    ADD COLUMN address_number VARCHAR(20) NULL AFTER street,
    ADD COLUMN address_complement VARCHAR(100) NULL AFTER address_number,
    ADD COLUMN neighborhood VARCHAR(100) NULL AFTER address_complement,
    ADD COLUMN postal_code VARCHAR(8) NULL AFTER neighborhood,
    ADD CONSTRAINT uk_users_cpf UNIQUE (cpf);

