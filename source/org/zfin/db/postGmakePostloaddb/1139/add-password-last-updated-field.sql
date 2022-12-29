--liquibase formatted sql
--changeset rtaylor:bcrypt

alter table zdb_submitters

add column password_last_updated timestamp(3) without time zone;


