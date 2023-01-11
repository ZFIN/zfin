--liquibase formatted sql
--changeset rtaylor:zfin-8396

alter table zdb_submitters

add column password_last_updated timestamp(3) without time zone;


