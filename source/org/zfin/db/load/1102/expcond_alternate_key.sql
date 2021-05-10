--liquibase formatted sql
--changeset sierra:expcond_alternate_key.sql

alter table experiment_condition
  add constraint experiment_condition_alternate_key unique (expcond_exp_zdb_id, 
                         expcond_zeco_term_zdb_id,
                         expcond_ao_term_zdb_id,
                         expcond_go_cc_term_zdb_id,
                         expcond_chebi_term_zdb_id,
                         expcond_taxon_term_zdb_id);

