begin work;

create table feature_protein_mutation_detail(fpmd_zdb_id varchar(50) not null constraint fpmd_zdb_id_not_null,
       	     			      fpmd_feature_zdb_id varchar(50) not null constraint fpmd_feature_zdb_id_not_null,
				      fpmd_sequence_of_reference_accession_number varchar(30),
				      fpmd_fdbcont_zdb_id varchar(50), -- can not be null if sequence of reference is not null
				      fpmd_protein_position_start int8,
				      fpmd_protein_position_end int8,
				      fpmd_wt_protein_term_zdb_id varchar(50),
				      fpmd_mutant_or_stop_protein_term_zdb_id varchar(50),
				      fpmd_number_amino_acids_removed int8,
				      fpmd_number_amino_acids_added int8,
				      fpmd_protein_consequence_term_zdb_id varchar(50),
		check (
			(fpmd_sequence_of_reference_accession_number is not null and 
			 fpmd_fdbcont_zdb_id is not null) or 
			(fpmd_sequence_of_reference_accession_number is null and 
			 fpmd_fdbcont_zdb_id is null)
			 )
) fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096
lock mode row;

create index feature_mutation_detail_protein_Consequenc_term_zdb_id_foreign_key_index
 on feature_protein_mutation_detail (fpmd_protein_consequence_term_zdb_id)
 using btree in idxdbs3;

create unique index fpmd_zdb_id_primary_key_index
 on feature_protein_mutation_detail (fpmd_zdb_id)
 using btree in idxdbs1;

create unique index fpmd_feature_zdb_id_alternate_key_index
 on feature_protein_mutation_detail (fpmd_feature_zdb_id)
 using btree in idxdbs2;

create index fpmd_fdbcont_zdb_id_foreign_key_index
 on feature_protein_mutation_detail (fpmd_fdbcont_zdb_id)
 using btree in idxdbs3;

create index fpmd_wt_protein_term_zdb_id_foreign_key_index
 on feature_protein_mutation_detail (fpmd_wt_protein_term_zdb_id)
 using btree in idxdbs3;

create index fpmd_mutant_or_stop_protein_term_zdb_id_foreign_key_index
 on feature_protein_mutation_detail (fpmd_mutant_or_stop_protein_term_zdb_id)
 using btree in idxdbs3;

alter table feature_protein_mutation_detail
 add constraint primary key (fpmd_zdb_id)
 constraint feature_protein_mutation_Detail_primary_key;

alter table feature_protein_mutation_detail
 add constraint unique (fpmd_feature_zdb_id)
 constraint feature_protein_mutation_Detail_alternate_key;

alter table feature_protein_mutation_detail
 add constraint (foreign key (fpmd_protein_consequence_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint
 fpmd_protein_consequence_term_zdb_id_fk_constraint);

alter table feature_protein_mutation_detail
 add constraint (foreign key (fpmd_fdbcont_zdb_id)
 references foreign_db_contains constraint
 fpmd_fdbcont_zdb_id_fk_constraint);

alter table feature_protein_mutation_detail
 add constraint (foreign key (fpmd_mutant_or_stop_protein_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint
 fpmd_mutant_or_stop_protein_term_zdb_id_fk_constraint);

alter table feature_protein_mutation_detail
 add constraint (foreign key (fpmd_feature_zdb_id)
 references feature constraint
 fpmd_feature_zdb_id_fk_constraint);

alter table feature_protein_mutation_detail
 add constraint (foreign key (fpmd_wt_protein_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint
 fpmd_wt_protein_term_zdb_id_fk_constraint);



--create trigger feature_protein_mutation_detail_update_trigger
--  update of fpmd_feature_zdb_id on feature_protein_mutation_detail
--  referencing old as old_fpmd new as new_fpmd
--    for each row (  
--    	execute procedure checkFeatureMutationDetail (new_fpmd.fpmd_feature_zdb_id)
--);

--create trigger feature_protein_mutation_detail_insert_trigger insert on feature_protein_mutation_detail
--  referencing new as new_fpmd
--    for each row (  
--    	execute procedure checkFeatureMutationDetail (new_fpmd.fpmd_feature_zdb_id)
 --);



insert into zdb_object_type (zobjtype_name, 
       	    		     zobjtype_day,
			     zobjtype_app_page,
			     zobjtype_home_table,
			     zobjtype_home_zdb_id_column,
			     zobjtype_is_data,
			     zobjtype_is_source)
 values ('FDMD',current,'feature_dna_mutation_detail','feature_dna_mutation_detail','fdmd_zdb_id','t','f');

create sequence fdmd_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;
alter sequence fdmd_seq restart with 1;



insert into zdb_object_type (zobjtype_name, 
       	    		     zobjtype_day,
			     zobjtype_app_page,
			     zobjtype_home_table,
			     zobjtype_home_zdb_id_column,
			     zobjtype_is_data,
			     zobjtype_is_source)
 values ('FPMD',current,'feature_protein_mutation_detail','feature_protein_mutation_detail','fpmd_zdb_id','t','f');

create sequence fpmd_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;
alter sequence fpmd_seq restart with 1;


insert into zdb_object_type (zobjtype_name, 
       	    		     zobjtype_day,
			     zobjtype_app_page,
			     zobjtype_home_table,
			     zobjtype_home_zdb_id_column,
			     zobjtype_is_data,
			     zobjtype_is_source)
 values ('FTMD',current,'feature_transcript_mutation_detail','feature_transcript_mutation_detail','ftmd_zdb_id','t','f');

create sequence ftmd_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;
alter sequence ftmd_seq restart with 1;


commit work
--rollback work;
