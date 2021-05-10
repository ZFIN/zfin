--liquibase formatted sql
--changeset pkalita:CUR-744

ALTER TABLE publication
    RENAME COLUMN pub_last_sent_email_date TO pub_last_correspondence_date;
