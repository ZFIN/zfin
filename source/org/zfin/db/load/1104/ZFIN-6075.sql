--liquibase formatted sql
--changeset sierra:ZFIN-6075

create table gene_description (gd_pk_id serial8 primary key,
                               gd_gene_zdb_id text unique not null,
                               gd_go_description text,
                               gd_go_function_description text,
                               gd_go_process_description text,
                               gd_go_component_description text,
                               gd_do_description text,
                               gd_do_experimental_description text,
                               gd_do_biomarker_description text,
                               gd_do_orthology_description text,
                               gd_orthology_description text,
                               gd_description text)
;
