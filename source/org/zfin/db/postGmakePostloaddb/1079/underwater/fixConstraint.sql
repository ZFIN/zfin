--liquibase formatted sql
--changeset sierra:fixConstraint

create unique index mdcv_zdb_id_pk_index
 on mutation_detail_controlled_vocabulary (mdcv_term_zdb_id)
 using btree in idxdbs2;

alter table mutation_detail_Controlled_vocabulary
 add constraint primary key (mdcv_term_zdb_id)
 constraint mdcv_pk;

alter table mutation_detail_controlled_vocabulary
 modify (mdcv_term_display_name varchar(255) not null constraint mdcv_term_display_name_not_null);

alter table mutation_detail_controlled_vocabulary
 modify (mdcv_used_in varchar(100) not null constraint mdcv_term_used_in_not_null);

alter table mutation_detail_controlled_vocabulary
 add constraint (foreign key (mdcv_term_zdb_id) 
 references term constraint mdcv_term_zdb_id_fk);

alter table feature_transcript_mutation_detail 
  add constraint (foreign key (ftmd_transcript_consequence_term_zdb_id)
  references mutation_detail_controlled_vocabulary constraint ftmd_mdcv_fk);

alter table feature_dna_mutation_detail 
  add constraint (foreign key (fdmd_dna_mutation_term_zdb_id)
  references mutation_detail_controlled_vocabulary constraint fdmd_mdcv_fk);


alter table feature_protein_mutation_detail 
  add constraint (foreign key (fpmd_protein_consequence_term_zdb_id)
  references mutation_detail_controlled_vocabulary constraint fpmd_mdcv_fk);
