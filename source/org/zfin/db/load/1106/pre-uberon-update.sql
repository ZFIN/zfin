--liquibase formatted sql
--changeset sierra:pre-uberon-update.sql

create table tmp_uberon_map (u_uberon_id text,
       u_zfa_id text);
