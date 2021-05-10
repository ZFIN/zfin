--liquibase formatted sql
--changeset pkalita:INF-3333

delete from external_note where extnote_note = ' ';