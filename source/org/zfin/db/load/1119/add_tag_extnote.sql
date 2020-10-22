--liquibase formatted sql
--changeset pm:add_tag_ext_note.sql

alter table external_note
add column extnote_tag text;






