--liquibase formatted sql
--changeset sierra:chebiTaxon

alter table experiment_condition
 add (expcond_chebi_term_zdb_id varchar(50));

alter table experiment_condition
 add (expcond_taxon_term_zdb_id varchar(50));

