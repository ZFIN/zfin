--liquibase formatted sql
--changeset cmpich:ZFIN-8856.sql

create table mesh_chebi_mapping
(
    mcm_id           serial not null,
    mcm_mesh_id      text   not null,
    mcm_mesh_name    text   not null,
    mcm_chebi_id     text   not null,
    mcm_cas_id       text   not null,
    mcm_cas_name     text,
    mcm_date_created timestamp default CURRENT_TIMESTAMP
);


ALTER TABLE mesh_chebi_mapping
    ADD CONSTRAINT mesh_chebi_mapping_chebi_fk1
        FOREIGN KEY (mcm_chebi_id)
            REFERENCES term (term_ont_id);