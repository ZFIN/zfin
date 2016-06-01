begin work;

create table feature_transcript_mutation_detail (ftmd_zdb_id varchar(50) not null constraint ftmd_zdb_id_not_null,
				       ftmd_transcript_consequence_term_zdb_id varchar(50) not null constraint ftmd_consequence_term_zdb_id_not_null,
				       ftmd_feature_zdb_id varchar(50) not null constraint ftmd_feature_zdb_id_not_null,
				       ftmd_exon_number int8,
				       ftmd_intron_number int8)
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096;
			
create unique index ftmd_primary_key_index
 on feature_transcript_mutation_detail (ftmd_zdb_id)
 using btree in idxdbs1;

create unique index ftmd_altetranscriptte_key_index
 on feature_transcript_mutation_detail (ftmd_feature_zdb_id,ftmd_transcript_consequence_term_zdb_id)
 using btree in idxdbs2;


create index ftmd_transcript_consequence_term_Zdb_id_fk_index
  on feature_transcript_mutation_detail (ftmd_transcript_consequence_term_zdb_id)
 using btree in idxdbs3;


alter table feature_transcript_mutation_detail 
  add constraint primary key (ftmd_zdb_id)
  constraint feature_transcript_mutation_detail_primary_key;

alter table feature_transcript_mutation_detail
 add constraint (foreign key (ftmd_feature_zdb_id)
 references feature on delete cascade constraint ftmd_feature_Zdb_id_fk_odc);

alter table feature_transcript_mutation_detail
 add constraint (foreign key (ftmd_zdb_id)
 references zdb_active_Data on delete cascade constraint ftmd_zdb_id_fk_odc);

alter table feature_transcript_mutation_detail
 add constraint unique (ftmd_feature_zdb_id, ftmd_transcript_consequence_term_Zdb_id)
 constraint feature_transcript_mutation_detail_altetranscriptte_key;

alter table feature_transcript_mutation_detail
 add constraint (foreign key (ftmd_transcript_consequence_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint ftmd_transcript_consequence_term_Zdb_id_fk);


commit work;

--rollback work;

