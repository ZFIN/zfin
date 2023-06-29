--liquibase formatted sql
--changeset cmpich:ZFIN-8674.sql


ALTER TABLE transcript_sequence
    ADD COLUMN created_at TIMESTAMP DEFAULT NOW();
