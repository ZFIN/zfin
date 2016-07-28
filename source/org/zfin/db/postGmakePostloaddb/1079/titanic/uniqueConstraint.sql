--liquibase formatted sql
--changeset sierra:uniqueConstraint

create unique index expcond_alternate_key_index
  on experiment_condition (expcond_exp_zdb_id, expcond_zeco_term_zdb_id,
     			 		      expcond_ao_term_zdb_id,
					      expcond_go_cc_term_zdb_id,
					      expcond_chebi_term_zdb_id,
					      expcond_taxon_term_zdb_id)
using btree in idxdbs3;

alter table experiment_condition
 add constraint unique (expcond_exp_zdb_id, expcond_zeco_term_zdb_id,
     			 		      expcond_ao_term_zdb_id,
					      expcond_go_cc_term_zdb_id,
					      expcond_chebi_term_zdb_id,
					      expcond_taxon_term_zdb_id)
  constraint experiment_condition_alternate_key_index);
