--liquibase formatted sql
--changeset kschaper:PLC-165.sql

alter table zdb_submitters

add column password_reset_key text,
add column password_reset_date timestamp(3) without time zone;


