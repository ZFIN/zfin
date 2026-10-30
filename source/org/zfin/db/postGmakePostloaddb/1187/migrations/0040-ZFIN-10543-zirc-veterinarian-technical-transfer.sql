--liquibase formatted sql

-- ZFIN-10543: Additional Info on the line submission form gains two contact
-- subsections, Veterinarian and Technical Transfer, each with name, email,
-- address and phone. Free text, all optional; stored flat on the submission
-- like the other Additional Info fields.

--changeset cmpich:ZFIN-10543-zirc-veterinarian-technical-transfer
ALTER TABLE zirc.line_submission
    ADD COLUMN ls_veterinarian_name               VARCHAR(255),
    ADD COLUMN ls_veterinarian_email              VARCHAR(255),
    ADD COLUMN ls_veterinarian_address            TEXT,
    ADD COLUMN ls_veterinarian_phone              VARCHAR(255),
    ADD COLUMN ls_technical_transfer_name         VARCHAR(255),
    ADD COLUMN ls_technical_transfer_email        VARCHAR(255),
    ADD COLUMN ls_technical_transfer_address      TEXT,
    ADD COLUMN ls_technical_transfer_phone        VARCHAR(255);
