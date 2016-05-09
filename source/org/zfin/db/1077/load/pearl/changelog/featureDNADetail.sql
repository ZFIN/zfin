

create table feature_dna_mutation_detail (fdmd_zdb_id varchar(50) not null constraint fdmd_zdb_id_not_null,
       	     			      fdmd_feature_zdb_id varchar(50) not null constraint fdmd_feature_zdb_id_not_null,
       	     			      fdmd_dna_mutation_term_zdb_id varchar(50),
				      fdmd_dna_sequence_of_reference_accession_number varchar(30), -- can not be null if sequence of reference is not null
				      fdmd_fdbcont_zdb_id varchar(50),
				      fdmd_dna_position_start int8,
				      fdmd_dna_position_end int8,
				      fdmd_number_additional_dna_base_pairs int8,
				      fdmd_number_removed_dna_base_pairs int8,
				      fdmd_exon_number int8, --splice_junction has both filled out
				      fdmd_intron_number int8, --splice_donor is for intron, spice_acceptor is for exon
				      fdmd_gene_localization_term_zdb_id varchar(50) --ie: splice_junction, spice_donor, splice_acceptor, promoter, 5UTR
	
				      )
fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 4096 next size 4096
lock mode row;

create unique index feature_dna_mutation_detail_primary_key_index 
  on feature_dna_mutation_detail (fdmd_zdb_id)
 using btree in idxdbs2;

create unique index feature_dna_mutation_detail_alternate_key_index 
  on feature_dna_mutation_detail (fdmd_feature_zdb_id)
 using btree in idxdbs2;


create index fdmd_fdbcont_zdb_id_fk_index
  on feature_dna_mutation_detail (fdmd_fdbcont_zdb_id)
 using btree in idxdbs3;

create index fdmd_dna_mutation_term_zdb_id_fk_index
  on feature_dna_mutation_detail (fdmd_dna_mutation_term_zdb_id)
 using btree in idxdbs1;

create index fdmd_gene_localization_term_zdb_id_fk_index
  on feature_dna_mutation_detail (fdmd_gene_localization_term_zdb_id)
 using btree in idxdbs3;

		     
alter table feature_dna_mutation_detail
 add constraint primary key (fdmd_zdb_id)
 constraint feature_dna_mutation_detail_primary_key;

alter table feature_dna_mutation_detail
 add constraint unique (fdmd_feature_zdb_id)
 constraint feature_dna_mutation_detail_alternate_key;

alter table feature_dna_mutation_detail
 add constraint (foreign key (fdmd_dna_mutation_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint
 fdmd_dna_mutation_term_zdb_id_foreign_key);

alter table feature_dna_mutation_detail
 add constraint (foreign key (fdmd_gene_localization_term_zdb_id)
 references mutation_detail_controlled_vocabulary constraint
 fdmd_gene_localization_term_zdb_id_foreign_key);

alter table feature_dna_mutation_detail
 add constraint (foreign key (fdmd_zdb_id)
 references zdb_active_data on delete cascade constraint
 fdmd_zdb_id_fk_odc);

alter table feature_dna_mutation_detail
 add constraint (foreign key (fdmd_fdbcont_zdb_id)
 references foreign_db_contains constraint
 fdmd_fdbcont_zdb_id_foreign_key);

alter table feature_dna_mutation_detail
 add constraint (foreign key (fdmd_feature_zdb_id)
 references feature constraint
 fdmd_feature_zdb_id_foreign_key);

create procedure checkFeatureMutationDetail (vFeatureZdbId varchar(50))

define vFeatureType like feature.feature_type;
define isAllele boolean;

let vFeatureType = (select feature_type from feature 
    		   	   where vFeatureZdbId = feature_zdb_id);
let isAllele ='f';

let isAllele = (select 't' from feature_marker_relationship, feature
 where vFeatureType = 'TRANSGENIC_INSERTION'
 and feature_Zdb_id = fmrel_ftr_zdb_id
 and fmrel_type = 'is allele of'
 and fmrel_ftr_zdb_id = vFeatureZdbId);

if vFeatureType not in ('INDEL','POINT_MUTATION','INSERTION','DELETION','DEFICIENCY', 'TRANSGENIC_INSERTION')
 then 
       raise exception -746,0,"FAIL!: feature mutation details can only be captured for insertion, deletion, indel, point and tgs";   
end if ;

if (vFeatureType = 'TRANSGENIC_INSERTION' and isAllele = 'f' ) 
  then
  raise exception -746,0,"FAIL!: feature mutation details can only be captured for tgs as alleles";   
end if ;

end procedure;



--create trigger feature_dna_mutation_detail_update_trigger
--  update of fdmd_feature_zdb_id on feature_dna_mutation_detail
--  referencing old as old_fdmd new as new_fdmd
--    for each row (  
--    	execute procedure checkFeatureMutationDetail (new_fdmd.fdmd_feature_zdb_id)
--);

--create trigger feature_dna_mutation_detail_insert_trigger insert on feature_dna_mutation_detail
--  referencing new as new_fdmd
--    for each row (  
--    	execute procedure checkFeatureMutationDetail (new_fdmd.fdmd_feature_zdb_id)
--);
