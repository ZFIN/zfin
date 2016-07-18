--liquibase formatted sql
--changeset sierra:addConstraints

create index expcond_chebi_term_zdb_id_fk_index
 on experiment_condition (expcond_chebi_term_zdb_id)
 using btree in idxdbs3;

create index expcond_ao_term_zdb_id_fk_index
 on experiment_condition (expcond_ao_term_zdb_id)
 using btree in idxdbs1;

create index expcond_go_cc_term_zdb_id_fk_index
 on experiment_condition (expcond_go_cc_term_zdb_id)
 using btree in idxdbs1;

create index expcond_taxon_term_zdb_id_fk_index
 on experiment_condition (expcond_taxon_term_zdb_id)
 using btree in idxdbs2;

alter table experiment_condition
 add constraint (Foreign key (expcond_chebi_term_zdb_id)
 references term constraint expcond_chebi_term_zdb_id_fk);

alter table experiment_condition
 add constraint (Foreign key (expcond_ao_term_zdb_id)
 references term constraint expcond_ao_term_zdb_id_fk);

alter table experiment_condition
 add constraint (Foreign key (expcond_taxon_term_zdb_id)
 references term constraint expcond_taxon_term_zdb_id_fk);
  
alter table experiment_condition
 add constraint (Foreign key (expcond_go_cc_term_zdb_id)
 references term constraint expcond_go_cc_term_zdb_id_fk);

