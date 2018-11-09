--liquibase formatted sql
--changeset sierra:add_ensdart_name_mapping.sql

create table ensdart_name_mapping (ensdart_stable_id text,
             ensdart_versioned_id text,
             ensdarg_id text,
             zfin_gene_zdb_id text,
             ensembl_tscript_name text,
             ottdart_id text);
