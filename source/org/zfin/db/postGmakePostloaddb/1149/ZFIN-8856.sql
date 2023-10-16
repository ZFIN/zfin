--liquibase formatted sql
--changeset cmpich:ZFIN-8856.sql

create table mesh_chebi_mapping
(
    mcm_id           serial not null,
    mcm_mesh_id      text   not null,
    mcm_chebi_id     text   not null,
    mcm_cas_id       text   not null,
    mcm_date_created timestamp default CURRENT_TIMESTAMP
);
