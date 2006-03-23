begin work ;

--still need:
create table feature_type (
	ftrtype_name	        varchar(30)
	  not null constraint ftrtype_name_not_null ,
	ftrtype_significance	integer,
	ftrtype_type_display	varchar(30)
	)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32 next size 32 lock mode row ;

set constraints all deferred ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('SEQUENCE_VARIANT', '9', 'Sequence Variant') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('POINT_MUTATION', '1','Point Mutant') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('INSERTION', '2','Insertion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('DELETION', '3','Deletion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('INTRACHROMOSOMAL_MUTATION', '4','Interchromosomal Mutation') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('INTERCHROMOSOMAL_MUTATION', '5','Interchromosomal Mutation') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('TRANSGENIC_INSERTION', '7','Transgenic Insertion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('LOCUS', '25', 'LOCUS') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('ALT', '25', 'Alelle') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LOCUS', '17','LOCUS') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TGCONSTRCT', '18', 'Transgenic Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('GTCONSTRCT', '19', 'Gene Trap Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PTCONSTRCT', '20', 'Promoter Trap Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('ETCONSTRCT', '21', 'Enhancer Trap Construct') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('REGION', '8','Special Feature') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('REGION', '22','Special Marker') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('GENEFAMILY', '23','Gene Family') ;

--new marker relationships for TG, ET, PT, and GT constructs

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('CONSTRUCT', 'this group contains genetically engineered constructs; promoter, enhancer, and gene traps, as well as trangenic construcs');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('REGION', 'this group contains genetic components like IRES; also refferd to as "special features."');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('TGCONSTRCT', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GTCONSTRCT', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('PTCONSTRCT', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('ETCONSTRCT', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('TGCONSTRCT', 'SEARCH_MKSEG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GTCONSTRCT', 'SEARCH_MKSEG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('PTCONSTRCT', 'SEARCH_MKSEG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('ETCONSTRCT', 'SEARCH_MKSEG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('REGION', 'REGION');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GENEFAMILY', 'GENEDOM');

insert into marker_relationship_type (mreltype_name, 
	mreltype_mrkr_type_group_1,
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
 values ('construct component promotes gene',
	 'CONSTRUCT',
	 'GENEDOM',
	 'has promoter',
	 'is promoter of');

insert into marker_relationship_type (mreltype_name, 
	mreltype_mrkr_type_group_1,
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
 values ('construct component is coding sequence of gene',
	 'CONSTRUCT',
	 'GENEDOM',
	 'has coding sequence',
	 'is coding sequence of');

insert into marker_relationship_type (mreltype_name, 
	mreltype_mrkr_type_group_1,
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
 values ('construct component contains region',
	 'CONSTRUCT',
	 'REGION',
	 'Contains',
	 'Contained in');		

create unique index feature_type_primary_key_index
  on feature_type (ftrtype_name)
  using btree in idxdbs3 ;

alter table feature_type 
  add constraint primary key (ftrtype_name)
  constraint feature_type_primary_key ;


--create feature_type_group
create table feature_type_group (ftrgrp_name varchar(30) not null constraint ftrgrp_name_not_null,
				ftrgrp_comments lvarchar not null constraint ftrgrp_comments_not_null)
 
in tbldbs3 
extent size 8 next size 8 ;

create unique index feature_type_group_primary_key_index
  on feature_type_group (ftrgrp_name) using btree
  in idxdbs3 ;

alter table feature_type_group
  add constraint primary key (ftrgrp_name)
  constraint feature_type_group_primary_key ;


insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('MUTANT', 
	 'all valid allele types in this group');


--create feature_type_group_member

create table feature_type_group_member (
	ftrgrpmem_ftr_type	varchar(30)
		not null constraint ftrgrpmem_ftr_type_not_null,
	ftrgrpmem_ftr_type_group varchar(30)
		not null constraint ftrgrpmem_ftr_type_group_not_null
	)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32 next size 32 lock mode row ; 


create unique index feature_type_group_member_primary_key_index
  on feature_type_group_member (ftrgrpmem_ftr_type,
  ftrgrpmem_ftr_type_group)
  using btree
  in idxdbs4 ;

create index feature_type_group_member_type_foreign_key_index 
  on feature_type_group_member (ftrgrpmem_ftr_type)
  using btree
  in idxdbs4 ;

alter table feature_type_group_member 
  add constraint primary key (ftrgrpmem_ftr_type, 
  ftrgrpmem_ftr_type_group) constraint
  feature_type_group_member_primary_key ;

alter table feature_type_group_member 
  add constraint (foreign key (ftrgrpmem_ftr_type)
  references feature_type constraint
  feature_type_group_member_type_foreign_key) ;

alter table feature_type_group_member add constraint 
    (foreign key (ftrgrpmem_ftr_type_group) references
	feature_type_group constraint ftrgrpmem_ftr_type_group_foreign_key);

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('SEQUENCE_VARIANT','MUTANT') ;

--insert into feature_type_group_member (ftrgrpmem_ftr_type,
--		ftrgrpmem_ftr_type_group)
--  values ('LOCUS','GENEDOM') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('LOCUS','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('ALT','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('POINT_MUTATION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INSERTION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('DELETION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INTERCHROMOSOMAL_MUTATION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSGENIC_INSERTION','MUTANT') ;

select ftrgrpmem_ftr_type
  from feature_type_group_member
  where ftrgrpmem_ftr_type not in (select ftrtype_name from feature_type);

--create feature_realtionship_type table

create table feature_marker_relationship_type (
	fmreltype_name	varchar(60) 
		not null constraint fmreltype_name_not_null,
	fmreltype_ftr_type_group varchar(30)
		not null constraint fmreltype_group1_not_null,
	fmreltype_mrkr_type_group varchar(20)
		not null constraint fmreltype_group2_not_null,
	fmreltype_1_to_2_comments varchar(255),
	fmreltype_2_to_1_comments varchar(255)
	)
in tbldbs1
extent size 16 next size 16 lock mode row ;

create unique index feature_marker_relationship_type_primary_key_index
  on feature_marker_relationship_type (fmreltype_name)
  using btree
  in idxdbs2 ;

create index feature_marker_relationship_type_ftr_group_index
  on feature_marker_relationship_type (fmreltype_ftr_type_group)
  using btree in idxdbs2 ;

create index feature_marker_relationship_type_mrkr_group_index
  on feature_marker_relationship_type (fmreltype_mrkr_type_group)
  using btree in idxdbs2 ;

alter table feature_marker_relationship_type
  add constraint primary key (fmreltype_name)
  constraint feature_marker_relationship_type_primary_key ;

insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('sequence variant contains sequence feature', 'MUTANT','MUTANT','Contains','Contained in');

insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('Mutation is allele of gene', 'MUTANT','GENEDOM','Is Allele of','Has Allele');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('LOCUS', 'MUTANT');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('TGCONSTRCT', 'MUTANT');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('PTCONSTRCT', 'MUTANT');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('ETCONSTRCT', 'MUTANT');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('GTCONSTRCT', 'MUTANT');

--for cases where cloned_gene is not null

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('LOCUS', 'GENEDOM');


--create table feature_marker_relationship

create table feature_marker_relationship (
	fmrel_zdb_id		varchar(50)
	  not null constraint fmrel_zdb_id_not_null,
        fmrel_type		varchar(60)
	  not null constraint fmrel_type_not_null,
	fmrel_ftr_zdb_id   varchar(50)
	  not null constraint fmrel_ftr1_not_null,
	fmrel_mrkr_zdb_id	varchar(50)
	  not null constraint fmrel_ftr2_not_null	
	)
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
  extent size 2048 next size 2048 lock mode row ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('FMREL', '11/15/2005','1','','feature_marker_relationship', 
	  'fmrel_zdb_id', 't','f', '2') ;


create unique index feature_marker_relationship_primary_key_index
  on feature_marker_relationship (fmrel_zdb_id) using btree
  in idxdbs4 ;

create unique index feature_marker_relationship_alternate_key_index 
  on feature_marker_relationship (fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id,
  fmrel_type) using btree  
  in idxdbs4 ;

create index fmrel_ftr_zdb_id_index 
  on feature_marker_relationship (fmrel_ftr_zdb_id) using btree 
  in idxdbs2 ;

create index fmrel_mrkr_zdb_id_index 
  on feature_marker_relationship (fmrel_mrkr_zdb_id) using btree 
  in idxdbs2 ;

create index fmrel_type_index 
  on feature_marker_relationship (fmrel_type) using btree 
  in idxdbs2 ;

alter table feature_marker_relationship
  add constraint unique 
  (fmrel_ftr_zdb_id, fmrel_mrkr_zdb_id, fmrel_type) constraint 
  feature_marker_relationship_alternate_key ;

alter table feature_marker_relationship 
  add constraint primary key (fmrel_zdb_id) 
  constraint feature_marker_relationship_primary_key ;

alter table feature_marker_relationship add constraint (foreign 
    key (fmrel_zdb_id) references zdb_active_data  on 
    delete cascade constraint fmrel_zdb_id_foreign_key);
    

alter table feature_marker_relationship add constraint (foreign 
    key (fmrel_type) references feature_marker_relationship_type 
     constraint fmrel_type_foreign_key);


create trigger feature_marker_relationship_insert_trigger 
    insert on feature_marker_relationship referencing new as 
    new_ftr_rel
    for each row
        (
        execute procedure p_fmrel_grpmem_correct(new_ftr_rel.fmrel_ftr_zdb_id,
		new_ftr_rel.fmrel_mrkr_zdb_id,
		new_ftr_rel.fmrel_type ));

create trigger feature_marker_relationship_update_trigger 
    update of fmrel_ftr_zdb_id,fmrel_mrkr_zdb_id,fmrel_type 
    on feature_marker_relationship referencing new as new_ftr_rel
    for each row
        (
        execute procedure p_fmrel_grpmem_correct(
		new_ftr_rel.fmrel_ftr_zdb_id,
		new_ftr_rel.fmrel_mrkr_zdb_id,
		new_ftr_rel.fmrel_type ));

create table feature (
	feature_zdb_id		 varchar(50)
		not null constraint feature_zdb_id_not_null,
	feature_name		 varchar(255) 	
		not null constraint feature_name_not_null,
	feature_abbrev		 varchar(40) 	
		not null constraint feature_abbrev_not_null,
        feature_type		 varchar(30) 	
		not null constraint feature_type_not_null,
	feature_lab_of_origin    varchar(50),
	feature_comments	 lvarchar,
	feature_name_order 	 varchar(140)	
		not null constraint feature_name_order_not_null,
	feature_abbrev_order	 varchar(140)
		not null constraint feature_abrv_order_not_null,
	feature_date_entered	 datetime year to second
		default current year to second
	)

fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9664 next size 9664 lock mode row ;

create trigger feature_insert_trigger insert on feature
  referencing new as new_feature
    for each row
        (
        execute function zero_pad(new_feature.feature_name 
    ) into feature.feature_name_order,
        execute function zero_pad(new_feature.feature_abbrev 
    ) into feature.feature_abbrev_order
);

create trigger feature_abbrev_update_trigger update of 
    feature_abbrev on feature referencing old as oldf new 
    as newf
    for each row
        (
        execute function zero_pad(newf.feature_abbrev ) 
	into feature.feature_abbrev_order
);

create trigger feature_name_update_trigger update of 
    feature_name on feature referencing old as oldf new 
    as newf
      for each row
          (execute function zero_pad(newf.feature_name ) 
      into feature.feature_name_order
);

create unique index feature_primary_key_index 
  on feature (feature_zdb_id) using btree 
  in idxdbs3 ;

create index feature_lab_of_origin_foreign_key_index 
  on feature (feature_lab_of_origin) using btree
  in idxdbs3 ;


create unique index feature_name_index on feature 
    (feature_name) using btree in idxdbs3 ;

create index feature_type_foreign_key_index on feature 
    (feature_type) using btree in idxdbs2 ;

alter table feature add constraint unique (feature_name) 
    constraint feature_name_unique  ;

alter table feature add constraint 
    (foreign key (feature_lab_of_origin) 
      references lab constraint feature_lab_of_origin_foreign_key)  ;

alter table feature add constraint unique (feature_abbrev) 
    constraint feature_abbrev_unique  ;

alter table feature 
  add constraint primary key (feature_zdb_id) constraint 
  feature_primary_key ;


alter table feature_marker_relationship add constraint (foreign 
    key (fmrel_ftr_zdb_id) references feature on 
    delete cascade constraint fmrel_ftr_zdb_id_foreign_key);
    
alter table feature_marker_relationship add constraint (foreign 
    key (fmrel_mrkr_zdb_id) references marker on 
    delete cascade constraint fmrel_mrkr_zdb_id_foreign_key);
    
drop trigger feature_insert_trigger;

drop trigger feature_name_update_trigger;

drop trigger feature_abbrev_update_trigger;

create trigger feature_insert_trigger insert on feature
  referencing new as new_feature
    for each row
        (
        execute function zero_pad(new_feature.feature_name 
    ) into feature.feature_name_order,
        execute function zero_pad(new_feature.feature_abbrev 
    ) into feature.feature_abbrev_order,
       execute procedure fhist_event(new_feature.feature_zdb_id,
       'assigned', new_feature.feature_name,new_feature.feature_abbrev) 
);

create trigger feature_abbrev_update_trigger update of 
    feature_abbrev on feature referencing old as oldf new 
    as newf
    for each row
        (execute function zero_pad(newf.feature_abbrev) 
	into feature.feature_abbrev_order
);

create trigger feature_name_update_trigger update of 
    feature_name on feature referencing old as oldf new 
    as newf
      for each row
          (execute function zero_pad(newf.feature_name ) 
      into feature.feature_name_order,
         execute procedure fhist_event(newf.feature_zdb_id,
		'renamed',
		newf.feature_name ,oldf.feature_name) 
);


--create table genotype

create table genotype (geno_zdb_id varchar(50) not null constraint 
				geno_zdb_id_not_null,
			geno_display_name varchar(100) not null constraint 
				geno_name_not_null,
			geno_handle varchar(100) not null constraint 
				geno_abbrev_not_null,
			geno_supplier_stock_number varchar(100),
			geno_date_entered datetime year to fraction(3) default 
				current year to fraction(3),
			geno_name_order varchar(50),
			geno_is_wildtype boolean default 'f'
)

fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024 lock mode row;

create unique index genotype_primary_key_index
  on genotype (geno_zdb_id)
  using btree in idxdbs4 ;

create unique index genotype_alternate_key_index
  on genotype (geno_display_name)
  using btree in idxdbs4 ;

create unique index genotype_handle_alternate_key_index
  on genotype (geno_handle)
  using btree in idxdbs4 ;

alter table genotype 
  add constraint primary key (geno_zdb_id)
  constraint genotype_primary_key ;

alter table genotype
  add constraint unique (geno_display_name) 
  constraint genotype_alternate_key ;

alter table genotype
  add constraint unique (geno_handle) 
  constraint genotype_handle_alternate_key ;

alter table genotype
  add constraint (foreign key (geno_zdb_id)
  references zdb_active_data on delete cascade
  constraint genotype_zdb_active_data_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENO', '11/15/2005','1','','genotype', 
	  'geno_zdb_id', 't','f', '2') ;


--create table zygocity (cv table)

create table zygocity (zyg_zdb_id varchar(50) not null constraint
			zygocity_zdb_id_not_null,
		       zyg_name  varchar(100) not null constraint
			zygocity_name_not_null,
		       zyg_definition varchar(255) not null constraint
			zygocity_definition_not_null)
in tbldbs2 extent size 8 next size 8 lock mode row ;

create unique index zygocity_primary_key_index
  on zygocity (zyg_zdb_id)
  using btree in idxdbs2 ;

create unique index zygocity_alternate_key_index
  on zygocity (zyg_name)
  using btree in idxdbs2 ;

alter table zygocity 
  add constraint primary key (zyg_zdb_id)
  constraint zygocity_primary_key ;

alter table zygocity
  add constraint unique (zyg_name)
  constraint zygocity_alternate_key ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('ZYG', '11/15/2005','1','','zygocity', 
	  'zyg_zdb_id', 't','f', '2') ;

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'homozygous', 'two copies of the allele at a given locus');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'heterozygous', 'one copy of the allele at a given locus');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'hemizygous', 'insertional construct on one chromosome only: The state of a gene present in only one copy in a diploid cell, such as a gene on the X chromosome in a male mammal, or a gene whose homologue has been deleted');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'maternal zygotic', 'mom and self homozygous');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'paternal zygotic', 'dad and self homozygous');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'complex', 'genotype with multiple insertions of insertional construct');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'unknown', 'zygocity unknown');

insert into zygocity (zyg_zdb_id, zyg_name, zyg_definition)
  values (get_id('ZYG'), 'wild type', 'parental zygocity is non-mutant');

--create genotype_feature table 

create table genotype_feature (genofeat_zdb_id varchar(50) not null constraint
					genofeat_zdb_id_not_null,
				genofeat_geno_zdb_id varchar(50) not null constraint
					genofeat_geno_zdb_id_not_null, 
				genofeat_feature_zdb_id varchar(50) not null constraint
					genofeat_feature_zdb_id_not_null,
				genofeat_chromosome varchar(2),
				genofeat_dad_zygocity varchar(50),
				genofeat_mom_zygocity varchar(50),
				genofeat_zygocity varchar(50) not null constraint
					genofeat_zygocity_not_null
)

fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 2048 next size 2048 lock mode row ;	

create unique index genotype_feature_primary_key_index
  on genotype_feature (genofeat_zdb_id)
  using btree in idxdbs3;

create unique index genotype_feature_alternate_key_index
  on genotype_feature (genofeat_geno_zdb_id, genofeat_feature_zdb_id, genofeat_chromosome) 
  using btree in idxdbs3;

create index genofeat_zygocity_index
  on genotype_feature (genofeat_zygocity)
  using btree in idxdbs3 ;

create index genofeat_mom_zygocity_index
  on genotype_feature (genofeat_mom_zygocity)
  using btree in idxdbs3 ;

create index genofeat_dad_zygocity_index
  on genotype_feature (genofeat_dad_zygocity)
  using btree in idxdbs3 ;

alter table genotype_feature
  add constraint primary key (genofeat_zdb_id)
  constraint genotype_feature_primary_key ;

alter table genotype_feature
  add constraint unique (genofeat_geno_zdb_id, genofeat_feature_zdb_id, genofeat_chromosome)
  constraint genotype_feature_alternate_key ;

alter table genotype_feature
  add constraint (foreign key (genofeat_geno_zdb_id) 
    references genotype on delete cascade constraint
    genofeat_geno_foreign_key_odc);

--! ODC from active data to genotype_feature.

alter table genotype_feature
   add constraint (foreign key (genofeat_zdb_id)
   references zdb_active_data on delete cascade constraint
   genofeat_zdb_active_data_foreign_key);

alter table genotype_feature
   add constraint (foreign key (genofeat_feature_zdb_id)
   references feature on delete cascade constraint
   genofeat_feature_foreign_key_odc);

alter table genotype_feature
   add constraint (foreign key (genofeat_chromosome)
   references linkage_group constraint genofeat_chromosome_foreign_key);

alter table genotype_feature
  add constraint (foreign key (genofeat_zygocity)
  references zygocity constraint 
	genofeat_zygocity_foreign_key);

alter table genotype_feature
  add constraint (foreign key (genofeat_dad_zygocity)
  references zygocity constraint 
	genofeat_dad_zygocity_foreign_key);

alter table genotype_feature
  add constraint (foreign key (genofeat_mom_zygocity)
  references zygocity constraint 
	genofeat_mom_zygocity_foreign_key);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENOFEAT', '11/15/2005','1','','genotype_feature', 
	  'genofeat_zdb_id', 't','f', '2') ;

--create genotype_marker table (a duplicate of the genotype_feature table
--so we don't have to merge the marker and feature tables).

create table genotype_marker (genomrkr_zdb_id varchar(50) not null constraint
					genomrkr_zdb_id_not_null,
				genomrkr_geno_zdb_id varchar(50) not null constraint
					genomrkr_geno_zdb_id_not_null, 
				genomrkr_mrkr_zdb_id varchar(50) not null constraint
					genomrkr_mrkr_zdb_id_not_null,	
				genomrkr_chromosome varchar(2),
				genomrkr_dad_zygocity varchar(50),
				genomrkr_mom_zygocity varchar(50),
				genomrkr_zygocity varchar(50) not null constraint
					genomrkr_zygocity_not_null
)

fragment by round robin in tbldbs1,tbldbs2,tbldbs3
extent size 2048 next size 2048 lock mode row ;	

create unique index genotype_marker_primary_key_index
  on genotype_marker (genomrkr_zdb_id)
  using btree in idxdbs3;

create unique index genotype_marker_alternate_key_index
  on genotype_marker (genomrkr_geno_zdb_id, genomrkr_mrkr_zdb_id) 
  using btree in idxdbs3;

create index genomrkr_zygocity_index
  on genotype_marker (genomrkr_zygocity)
  using btree in idxdbs3 ;

create index genomrkr_mom_zygocity_index
  on genotype_marker (genomrkr_mom_zygocity)
  using btree in idxdbs3 ;

create index genomrkr_dad_zygocity_index
  on genotype_marker (genomrkr_dad_zygocity)
  using btree in idxdbs3 ;

alter table genotype_marker
  add constraint (foreign key (genomrkr_zygocity)
  references zygocity constraint 
	genomrkr_zygocity_foreign_key);

alter table genotype_marker
  add constraint (foreign key (genomrkr_dad_zygocity)
  references zygocity constraint 
	genomrkr_dad_zygocity_foreign_key);

alter table genotype_marker
  add constraint (foreign key (genomrkr_mom_zygocity)
  references zygocity constraint 
	genomrkr_mom_zygocity_foreign_key);

alter table genotype_marker
  add constraint primary key (genomrkr_zdb_id)
  constraint genotype_marker_primary_key ;

alter table genotype_marker
  add constraint unique (genomrkr_geno_zdb_id, genomrkr_mrkr_zdb_id)
  constraint genotype_marker_alternate_key ;

alter table genotype_marker
  add constraint (foreign key (genomrkr_geno_zdb_id) 
    references genotype on delete cascade constraint
    genomrkr_geno_foreign_key_odc);

--! ODC from active data to genotype_marker.

alter table genotype_marker
   add constraint (foreign key (genomrkr_zdb_id)
   references zdb_active_data on delete cascade constraint
   genomrkr_zdb_active_data_foreign_key);

alter table genotype_marker
   add constraint (foreign key (genomrkr_mrkr_zdb_id)
   references marker on delete cascade constraint
   genomrkr_mrkr_foreign_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table,
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENOMRKR', '11/15/2005','1','','genotype_marker', 
	  'genomrkr_zdb_id', 't','f', '2');

--create table genotype_background

--genotype background table holds the background information
--for an experimental genotype.  a genotype can have more than 1 background,
--and a paper can specify if the background of interest was from the
--mother or the father of the genotype.
--genoback_background_gender is the gender of the background in the record.  
--Male, female are obvious.  null means it was not specified in the paper.


create table genotype_background (genoback_geno_zdb_id varchar(50), 
					genoback_background_zdb_id varchar(50))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024
lock mode row ;

create unique index genotype_background_primary_key_index
  on genotype_background (genoback_geno_zdb_id, 
				genoback_background_zdb_id)
  using btree in idxdbs3 ;

create index genotype_background_genotype_zdb_id_foreign_key_index
  on genotype_background (genoback_geno_zdb_id)
  using btree in idxdbs3 ;

create index genoback_genotype_zdb_id_foreign_key_index
  on genotype_background (genoback_background_zdb_id)
  using btree in idxdbs3 ;

alter table genotype_background
  add constraint (foreign key (genoback_geno_zdb_id)
  references genotype on delete cascade
  constraint genotype_background_geno_foreign_key_odc) ;

alter table genotype_background
  add constraint (foreign key (genoback_background_zdb_id)
  references genotype on delete cascade constraint
  genotype_background_background_foreign_key_odc) ;

alter table genotype_background 
  add constraint primary key (genoback_geno_zdb_id, 
				genoback_background_zdb_id)
  constraint genotype_background_primary_key ;

--create table feature_assay

--this is a one-to-one table with feature...it describes how
--mutant features were created.

create table feature_assay (featassay_feature_zdb_id varchar(50),
				featassay_mutagen varchar(20),
				featassay_mutagee varchar(20))
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 1024 next size 1024
lock mode row ;

create unique index feature_assay_primary_key_index
  on feature_assay (featassay_feature_zdb_id)
  using btree in idxdbs3 ;

alter table feature_assay
  add constraint primary key (featassay_feature_zdb_id)
  constraint feature_assay_primary_key ;

create index feature_assay_mutagen_foreign_key_index 
  on feature_assay (featassay_mutagen) 
  using btree in idxdbs3 ;

create index feature_assay_mutagee_foreign_key_index
  on feature_assay (featassay_mutagee)
  using btree in idxdbs3 ;

alter table feature_assay
  add constraint (foreign key (featassay_mutagen)
  references mutagen constraint feature_assay_mutagen_foreign_key );

alter table feature_assay
  add constraint (foreign key (featassay_mutagee)
  references mutagee constraint feature_assay_mutagee_foreign_key );

alter table feature_assay
  add constraint (foreign key (featassay_feature_zdb_id)
  references feature on delete cascade 
  constraint feature_assay_feature_zdb_id_foreign_key_odc);

--create the term table (could this wait till phase 2?)
--fuck it, lets make it generic at least with PATO.  

--create the generic ontology table

create table ontology ( 
    ont_ontology_name varchar(30),
    ont_order integer not null constraint ont_order_not_null
  )
in tbldbs3 extent size 8 next size 8;

create unique index ontology_primary_key_index 
    on ontology (ont_ontology_name) 
    using btree in idxdbs3;

alter table ontology add constraint primary key 
    (ont_ontology_name) constraint ontology_primary_key ;

insert into ontology
  values ('sequence ontology', '1');

insert into ontology
  values ('pato attribute ontology', '2');

insert into ontology
  values ('pato value ontology', '3');

insert into ontology
  values ('pato context ontology', '4');

create table term (
    term_zdb_id varchar(50),
    term_ont_id varchar(50) not null constraint term_goid_not_null,
    term_name varchar(255) not null constraint term_name_not_null,
    term_ontology varchar(30),
    term_is_obsolete boolean 
        default 'f' not null constraint term_is_obsolete_not_null,
    term_is_secondary boolean 
        default 'f' not null constraint term_is_secondary_not_null
  )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
  extent size 1028 next size 1028 lock mode page;

create unique index term_primary_key_index 
	on term (term_zdb_id) using btree in idxdbs3;
create unique index term_ont_id_index on 
    term (term_ont_id) using btree in idxdbs3 ;
create index term_ontology_index on term 
    (term_ontology) using btree in idxdbs3;

alter table term add constraint unique (term_ont_id) 
    constraint term_ont_id_unique  ;
alter table term add constraint primary key (term_zdb_id) 
    constraint term_primary_key  ;
alter table term add constraint (foreign key (term_ontology) 
    references ontology constraint term_ontology_foreign_key);
  
alter table term add constraint (foreign key (term_zdb_id) 
    references zdb_active_data  on delete cascade constraint 
    term_zdb_id_foreign_key);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('TERM', '11/15/2005','1','','term', 
	  'term_zdb_id', 't','f', '2') ;

--create table genotype_feature_experiment

create table genotype_experiment (genox_zdb_id varchar(50) not null constraint
						genox_zdb_id_not_null,
					  genox_geno_zdb_id varchar(50) not null constraint
						genox_geno_zdb_d_not_null,
					  genox_exp_zdb_id varchar(50) not null constraint
						genox_exp_zdb_id_not_null
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
  extent size 1024 next size 1024 ;

create unique index genotype_experiment_primary_key_index
  on genotype_experiment  (genox_zdb_id) 
  using btree in idxdbs3;

create unique index genotype_experiment_alternate_key_index
  on genotype_experiment (genox_geno_zdb_id, genox_exp_zdb_id)
  using btree in idxdbs2 ;

alter table genotype_experiment
  add constraint primary key (genox_zdb_id)
  constraint genotype_experiment_primary_key;

alter table genotype_experiment
  add constraint unique (genox_geno_zdb_id, genox_exp_zdb_id)
  constraint genotype_experiment_alternate_key;

alter table genotype_experiment
  add constraint (foreign key (genox_geno_zdb_id)
  references genotype on delete cascade constraint
  genotype_experiment_genotype_foregin_key_odc);

alter table genotype_experiment
  add constraint (foreign key (genox_exp_zdb_id)
  references experiment on delete cascade constraint
  genotype_experiment_exp_foregin_key_odc);

alter table genotype_experiment
  add constraint (foreign key (genox_zdb_id)
  references zdb_active_data on delete cascade constraint
  genotype_experiment_genox_foregin_key_odc);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENOX', '11/15/2005','1','','genotype_experiment', 
	  'genox_zdb_id', 't','f', '2') ;

--create table phenotype_hysterical_summary

--create table phenotype_old (pold_genox_zdb_id varchar(50) not null 
--						constraint phissum_genox_zdb_id_not_null,
--					  pold_submitter_comments lvarchar(6000),
--					  pold_segregation lvarchar 
--)
--fragment by round robin in tbldbs1, tbldbs2, tbldbs3
-- extent size 1024 next size 1024 lock mode row;

--create unique index phenotype_old_primary_key_index
--  on phenotype_old (pold_genox_zdb_id)
--  using btree in idxdbs2; 

--alter table phenotype_old
--  add constraint (foreign key (pold_genox_zdb_id)
--  references genotype_experiment on delete cascade
--  constraint pold_genox_zdb_id_foreign_key_odc);

--alter table phenotype_old
--  add constraint primary key (pold_genox_zdb_id)
--  constraint phenotype_old_primary_key ;

--create the phenotype_anatomy table

create table phenotype_anatomy (pato_zdb_id varchar(50) not null
				constraint pato_zdb_id_not_null,
			pato_genox_zdb_id varchar(50) not null
				constraint pato_genox_zdb_id_not_null,
			pato_start_stg_zdb_id varchar(50),
			pato_end_stg_zdb_id varchar(50),
			pato_entity_zdb_id varchar(50) not null constraint 
				pato_entity_zdb_id_not_null,
			pato_attribute_zdb_id varchar(50) 
				default 'qualitatitive'
				not null constraint pato_attribute_not_null ,
			pato_value_zdb_id varchar(50) default 'abnormal'
				not null constraint pato_value_not_null,
			pato_context varchar(50) default 'unspecified'
				not null constraint pato_context_not_null
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 1024 next size 1024 ;

create unique index phenotype_anatomy_primary_key_index
  on phenotype_anatomy (pato_zdb_id)
  using btree in idxdbs2 ;

create unique index phenotype_anatomy_alternate_key_index
  on phenotype_anatomy (pato_genox_zdb_id, 
			pato_start_stg_zdb_id, 
			pato_end_stg_zdb_id,
			pato_entity_zdb_id,
			pato_attribute_zdb_id,
			pato_value_zdb_id)
  using btree in idxdbs3;

create index pato_start_stg_fk_index 
  on phenotype_Anatomy (pato_start_stg_zdb_id)
  using btree in idxdbs2 ;

create index pato_end_stg_fk_index
  on phenotype_anatomy (pato_end_stg_zdb_id)
  using btree in idxdbs2 ;

alter table phenotype_anatomy
  add constraint primary key (pato_zdb_id)
  constraint phenotype_anatomy_primary_key ;

alter table phenotype_anatomy
  add constraint (foreign key (pato_zdb_id)
  references zdb_active_data on delete cascade
  constraint pato_zdb_active_data_foreign_key_odc);

alter table phenotype_anatomy
  add constraint unique (pato_genox_zdb_id, 
			pato_start_stg_zdb_id, 
			pato_end_stg_zdb_id,
			pato_entity_zdb_id,
			pato_attribute_zdb_id,
			pato_value_zdb_id)
  constraint phenotype_anatomy_alternate_key_constraint ;

alter table phenotype_anatomy
  add constraint (foreign key (pato_genox_zdb_id)
  references genotype_experiment on delete cascade
  constraint pato_genox_foreign_key_odc);

alter table phenotype_anatomy
  add constraint (foreign key (pato_start_stg_zdb_id)
  references stage
  constraint pato_start_stg_foreign_key);

alter table phenotype_anatomy
  add constraint (foreign key (pato_end_stg_zdb_id)
  references stage
  constraint pato_end_stage_foreign_key);

alter table phenotype_anatomy
  add constraint (foreign key (pato_entity_zdb_id)
  references anatomy_item
  constraint pato_entity_foreign_key);

alter table phenotype_anatomy
  add constraint (foreign key (pato_attribute_zdb_id)
  references term
  constraint pato_attribute_foreign_key);

alter table phenotype_anatomy
  add constraint (foreign key (pato_value_zdb_id)
  references term
  constraint pato_value_foreign_key);


--create the phenotype_go table

create table phenotype_go (patog_zdb_id varchar(50) not null
				constraint patog_zdb_id_not_null,
			patog_genox_zdb_id varchar(50) not null
				constraint patog_genox_zdb_id_not_null,
			patog_start_stg_zdb_id varchar(50),
			patog_end_stg_zdb_id varchar(50),
			patog_entity_zdb_id varchar(50) not null constraint 
				patog_entity_zdb_id_not_null,
			patog_attribute_zdb_id varchar(50) 
				default 'unspecified'
				not null constraint
				    patog_attribute_zdb_id_not_null,
			patog_value_zdb_id varchar(50) 
				default 'unspecified' 
				not null constraint
				    patog_value_zdb_id_not_null,
			patog_context varchar(50)
)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 1024 next size 1024 ;

create unique index phenotype_go_primary_key_index
  on phenotype_go (patog_zdb_id)
  using btree in idxdbs2 ;

create unique index phenotype_go_alternate_key_index
  on phenotype_go (patog_genox_zdb_id, 
			patog_start_stg_zdb_id, 
			patog_end_stg_zdb_id,
			patog_entity_zdb_id,
			patog_attribute_zdb_id,
			patog_value_zdb_id)
  using btree in idxdbs3;

alter table phenotype_go
  add constraint primary key (patog_zdb_id)
  constraint phenotype_go_primary_key ;

alter table phenotype_go
  add constraint unique (patog_genox_zdb_id, 
			patog_start_stg_zdb_id, 
			patog_end_stg_zdb_id,
			patog_entity_zdb_id,
			patog_attribute_zdb_id,
			patog_value_zdb_id)
  constraint phenotype_go_alternate_key_constraint ;

alter table phenotype_go
  add constraint (foreign key (patog_genox_zdb_id)
  references genotype_experiment on delete cascade
  constraint patog_genox_foreign_key_odc);

alter table phenotype_go
  add constraint (foreign key (patog_zdb_id)
  references zdb_active_data on delete cascade
  constraint patog_zdb_active_data_foreign_key_odc);

alter table phenotype_go
  add constraint (foreign key (patog_start_stg_zdb_id)
  references stage
  constraint patog_start_stg_foreign_key);

alter table phenotype_go
  add constraint (foreign key (patog_end_stg_zdb_id)
  references stage
  constraint patog_end_stage_foreign_key);

alter table phenotype_go
  add constraint (foreign key (patog_entity_zdb_id)
  references go_term
  constraint patog_entity_foreign_key);

alter table phenotype_go
  add constraint (foreign key (patog_attribute_zdb_id)
  references term
  constraint patog_attribute_foreign_key);

alter table phenotype_go
  add constraint (foreign key (patog_value_zdb_id)
  references term
  constraint patog_value_foreign_key);

--NEED TO UPDATE validate data to not delete phenotype_go
--zdb_ids as orphans!!!

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('PATO', '11/15/2005','1','','phenotype_anatomy', 
	  'pato_zdb_id', 't','f', '2') ;

--create table feature_history
create table feature_history 
  (
    fhist_zdb_id varchar(50),
    fhist_ftr_zdb_id varchar(50) not null constraint 
		fhist_ftr_zdb_id_not_null,
    fhist_event varchar(40) not null constraint 
		fhist_event_not_null,
    fhist_reason varchar(50),
    fhist_date datetime year to second not null constraint 
		fhist_date_not_null,
    fhist_ftr_name_on_fhist_date varchar(255) not null constraint 
		fhist_name_on_fhist_date_not_null,
    fhist_ftr_abbrev_on_fhist_date varchar(40) not null constraint 
	fhist_ftr_abbrev_on_fhist_date_not_null,
    fhist_comments lvarchar,
    fhist_dalias_zdb_id varchar(50),
    fhist_ftr_prev_name varchar(255)
  )
 fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
  extent size 2048 next size 2048 lock mode row;


create unique index feature_history_primary_key_index 
    on feature_history (fhist_zdb_id) 
    using btree in idxdbs2 ;

create index fhist_dalias_zdb_id_index 
    on feature_history (fhist_dalias_zdb_id) 
    using btree in idxdbs2;

create index fhist_event_index 
    on feature_history (fhist_event) 
    using btree in idxdbs2;

create index fhist_ftr_zdb_id_index 
    on feature_history (fhist_ftr_zdb_id) 
    using btree in idxdbs2;

create index fhist_reason_index 
    on feature_history (fhist_reason) 
    using btree in idxdbs2;

alter table feature_history add constraint primary 
    key (fhist_zdb_id) constraint feature_hist_primary_key ;

alter table feature_history add constraint (foreign 
    key (fhist_zdb_id) references zdb_active_data  
    on delete cascade constraint fhist_zdb_id_foreign_key_odc);
   
alter table feature_history add constraint (foreign 
    key (fhist_ftr_zdb_id) references feature  on delete 
    cascade constraint fhist_ftr_zdb_id_foreign_key_odc);
    

alter table feature_history add constraint (foreign 
    key (fhist_event) references marker_history_event 
     constraint fhist_event_foreign_key);

alter table feature_history add constraint (foreign 
    key (fhist_dalias_zdb_id) references data_alias 
     constraint fhist_dalias_zdb_id_foreign_key);

alter table feature_history add constraint (foreign 
    key (fhist_reason) references marker_history_reason 
     constraint fhist_reason_foreign_key);


--create table feature_sequence

--create table feature_sequence (
 --   featseq_zdb_id varchar(50) not null constraint 
--				featseq_zdb_id_not_null,
--    featseq_feat_zdb_id varchar(50) not null 
--				constraint featseq_feat_zdb_id_not_null,
--    featseq_sequence lvarchar not null 
--				constraint featseq_sequence_not_null,
--    featseq_left_end varchar(10) default '5'''
--  )

--fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
--extent size 64 next size 64 lock mode row;

--create unique index feature_sequence_primary_key_index 
--    on feature_sequence (featseq_zdb_id) using btree in idxdbs4 ;

--create index featseq_feat_zdb_id_foreign_key_index 
--    on feature_sequence (featseq_feat_zdb_id) using 
--    btree in idxdbs4 ;
--alter table feature_sequence add constraint primary 
--    key (featseq_zdb_id) constraint feature_sequence_primary_key ;

--alter table feature_sequence add constraint (foreign 
--    key (featseq_feat_zdb_id) references feature  on 
--    delete cascade constraint featseq_feat_zdb_id_feature_foreign_key);
    
--alter table feature_sequence add constraint (foreign 
--    key (featseq_zdb_id) references zdb_active_data 
--     on delete cascade constraint featseq_zdb_id_active_data_foreign_key);


--create table fish supplier status

alter table int_data_supplier
  add (idsup_avail_state varchar(30)) ;

update int_data_supplier
  set idsup_avail_state = (select fsupstat_fish_status
				from fish_supplier_status
				where fsupstat_supplier_zdb_id = 
					idsup_supplier_zdb_id
				and fsupstat_fish_zdb_id =
					idsup_data_zdb_id)
  where idsup_data_zdb_id like 'ZDB-FISH%';

--leftover constraints to avoid errors 

set constraints all immediate ;

alter table feature add constraint (foreign key (feature_type) 
    references feature_type constraint feature_type_foreign_key);

alter table feature_marker_relationship_type add constraint 
    (foreign key (fmreltype_ftr_type_group) references feature_type_group  
	constraint fmreltype_ftr_type_group_ftr_foreign_key);

--feature vs marker type group.

alter table feature_marker_relationship_type add constraint 
    (foreign key (fmreltype_mrkr_type_group) references marker_type_group 
	constraint fmreltype_ftr_type_group_mrkr_foreign_key);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('INSRTN', '11/15/2005','1','','feature', 
	  'feature_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('DLTN', '11/15/2005','1','','feature', 
	  'feature_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('REGION', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENEFAMILY', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('TGCONSTRCT', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('ETCONSTRCT', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('PTCONSTRCT', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GTCONSTRCT', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('SEQVAR', '11/15/2005','1','','feature', 
	  'feature_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('PNTMTN', '11/15/2005','1','','feature', 
	  'feature_zdb_id', 't','f', '2') ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('INTRCRMSLTN', '11/15/2005','1','','feature', 
	  'feature_zdb_id', 't','f', '2') ;

--create table pato_figure 

create table pato_figure (patofig_pato_zdb_id varchar(50) 
				not null constraint patofig_pato_zdb_id_not_null,
			     patofig_fig_zdb_id varchar(50)
				not null constraint patofig_fig_zdb_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 1024 next size 1024 lock mode row;

create unique index patofig_primary_key_index
  on pato_figure (patofig_pato_zdb_id, patofig_fig_zdb_id)
  using btree in idxdbs4 ;

create index patofig_fig_foreign_key_index
  on pato_figure (patofig_fig_zdb_id) 
  using btree in idxdbs4 ;

create index patofig_pato_zdb_id_foreign_key_index
  on pato_figure (patofig_pato_zdb_id)
  using btree in idxdbs4 ;

alter table pato_figure
  add constraint primary key (patofig_pato_zdb_id, patofig_fig_zdb_id)
  constraint pato_figure_primary_key ;

alter table pato_figure
  add constraint (foreign key (patofig_pato_zdb_id)
  references zdb_active_data on delete cascade
  constraint patofig_pato_foreign_key_odc) ;

alter table pato_figure
  add constraint (foreign key (patofig_fig_zdb_id)
  references figure on delete cascade
  constraint patofig_fig_foreign_key_odc) ;

--create table genotype_experiment_figure (genoxfig_genox_zdb_id varchar(50) not null 
--						constraint genoxfig_genox_zdb_id_not_null,
--					genoxfig_fig_zdb_id varchar(50) not null
--						constraint genoxfig_fig_zdb_id_not_null
--					)
--fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
--extent size 1024 next size 1024 lock mode row ;

--create unique index genotype_experiment_figure_primary_key_index
--  on genotype_experiment_figure (genoxfig_genox_zdb_id, genoxfig_fig_zdb_id)
--  using btree in idxdbs3 ;

--create index genoxfig_genox_zdb_id_foreign_key_index
--  on genotype_experiment_figure (genoxfig_genox_zdb_id) 
--  using btree in idxdbs3 ;

--create index genoxfig_exp_zdb_id_foreign_key_index
--  on genotype_experiment_figure (genoxfig_fig_zdb_id) 
--  using btree in idxdbs3 ;

--alter table genotype_experiment_figure
--  add constraint primary key (genoxfig_genox_zdb_id, genoxfig_fig_zdb_id)
--  constraint genotype_experiment_figure_primary_key ;

--alter table genotype_experiment_figure
--  add constraint (foreign key (genoxfig_genox_zdb_id) 
--  references genotype_experiment on delete cascade
--  constraint genoxfig_genox_foreign_key_odc);

--alter table genotype_experiment_figure
--  add constraint (foreign key (genoxfig_fig_zdb_id)
--  references figure on delete cascade
--  constraint genoxfig_fig_foreign_key_odc);






alter table figure
 drop constraint fx_figure_source_foreign_key ;

alter table figure
  add constraint (foreign key (fig_source_zdb_id)
  references zdb_active_source on delete cascade constraint
  figure_source_foreign_key_odc);

create temp table tmp_fish_image (fish_id varchar(50),
					owner varchar(50), 
					counter int)
with no log; 

insert into tmp_fish_image (fish_id, counter)
  select fimg_fish_zdb_id, count(*)
    from fish_image
    where fimg_fig_zdb_id is null
    group by fimg_fish_zdb_id ;

set constraints all deferred ;

alter table figure
  modify (fig_label varchar(50)) ;

insert into figure (fig_zdb_id,
			fig_label, fig_comments, fig_source_zdb_id)

select get_id('FIG'),
	fish_id,
        fish_id,
        'ZDB-PUB-030129-1'
  from tmp_fish_image;

update fish_image
  set fimg_fig_zdb_id = (select fig_zdb_id
				from figure
				where fimg_fish_zdb_id = fig_label)
  where fimg_fig_zdb_id is null;

alter table fish_image
  drop constraint fimg_fish_foreign_key_odc ;


insert into zdb_active_data
  select fig_Zdb_id from figure
	where fig_zdb_id not in (select zactvd_zdb_id from zdb_active_Data);

set constraints all immediate ;

create table image_view 
  (
    imgview_name varchar(20)
  ) in tbldbs2  extent size 8 next size 8 lock mode row;


create unique index image_view_primary_key_index 
    on image_view (imgview_name) using btree 
     in idxdbs4 ;
alter table image_view add constraint primary 
    key (imgview_name) constraint image_view_primary_key 
     ;

insert into image_view
  select * from fish_image_view ;

create table image_preparation 
  (
    imgprep_name varchar(15)
  ) in tbldbs2  extent size 8 next size 8 lock mode row;

create unique index image_preparation_primary_key_index 
    on image_preparation (imgprep_name) using 
    btree  in idxdbs4 ;
alter table image_preparation add constraint 
    primary key (imgprep_name) constraint image_preparation_primary_key ;

insert into image_preparation
  select * from fish_image_preparation ;

create table image_direction 
  (
    imgdir_name varchar(30)
  ) in tbldbs3  extent size 8 next size 8 lock mode row;

create unique index image_direction_primary_key_index 
    on image_direction (imgdir_name) using btree 
     in idxdbs4 ;
alter table image_direction add constraint primary 
    key (imgdir_name) constraint image_direction_primary_key ;

insert into image_direction
  select * from fish_image_direction ;

create table image_form 
  (
    imgform_name varchar(15)
  ) in tbldbs1  extent size 8 next size 8 lock mode row;

create unique index image_form_primary_key_index 
    on image_form (imgform_name) using btree 
     in idxdbs4 ;
alter table image_form add constraint primary 
    key (imgform_name) constraint image_form_primary_key ;

insert into image_form
  select * from fish_image_form ;

--also image direction
-- image form

create table image 
  (
    img_zdb_id varchar(50),
    img_fig_zdb_id varchar(50),
    img_label varchar(200),
    img_comments lvarchar,
    img_annotation lvarchar,
    img_width integer not null constraint img_width_not_null,
    img_height integer not null constraint img_height_not_null,
    img_view varchar(20) not null constraint img_view_not_null,
    img_direction varchar(30) not null constraint img_direction_not_null,
    img_form varchar(15) not null constraint img_form_not_null,
    img_preparation varchar(15) not null constraint img_preparation_not_null,
    img_owner_zdb_id varchar(50) not null constraint img_owner_zdb_id_not_null,
    img_external_name varchar(100),
    img_image varchar(150) not null constraint img_image_not_null,
    img_image_with_annotation varchar(150),
    img_thumbnail varchar(150) not null constraint img_thumbnail_not_null
  )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
 extent size 4096 next size 4096 lock mode row;

create index img_direction_index on image 
    (img_direction) using btree ;
create index img_form_index on image 
    (img_form) using btree in idxdbs2 ;
create index img_owner_zdb_id_index on image 
    (img_owner_zdb_id) using btree in idxdbs2 ;
create index img_preparation_index on image 
    (img_preparation) using btree in idxdbs2 ;
create index img_view_index on image 
    (img_view) using btree in idxdbs2 ;
create index image_figure_foreign_key_index on 
    image (img_fig_zdb_id) using btree in idxdbs2 ;
create index image_img_image_index 
	on image (img_image) using btree in idxdbs2 ;
create unique index image_primary_key_index on 
    image (img_zdb_id) using btree in idxdbs2 ;

alter table image add constraint primary key 
    (img_zdb_id) constraint image_primary_key ;

alter table image add constraint (foreign key 
    (img_fig_zdb_id) references figure  on delete 
    cascade constraint image_fig_foreign_key_odc);
    
alter table image add constraint (foreign key 
    (img_zdb_id) references zdb_active_data  on delete 
    cascade constraint img_zdb_id_foreign_key_odc);

alter table image add constraint (foreign key 
    (img_view) references image_view  constraint 
    img_view_foreign_key);

alter table image add constraint (foreign key 
    (img_direction) references image_direction 
     constraint img_direction_foreign_key);

alter table image add constraint (foreign key 
    (img_form) references image_form  constraint 
    img_form_foreign_key);

alter table image add constraint (foreign key 
    (img_preparation) references image_preparation 
     constraint img_preparation_foreign_key);

alter table image add constraint (foreign key 
    (img_owner_zdb_id) references person constraint 
    img_owner_zdb_id_foreign_key);

create table image_stage 
  (
    imgstg_img_zdb_id varchar(50),
    imgstg_start_stg_zdb_id varchar(50),
    imgstg_end_stg_zdb_id varchar(50)
  ) 
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
  extent size 1024 next size 1024 lock mode row;


create index imgstg_end_stg_zdb_id_index on 
    image_stage (imgstg_end_stg_zdb_id) using btree  in 
    idxdbs4 ;
create index imgstg_img_zdb_id_index on 
    image_stage (imgstg_img_zdb_id) using btree  in idxdbs4 
    ;
create index imgstg_start_stg_zdb_id_index on
    image_stage (imgstg_start_stg_zdb_id) using btree 
     in idxdbs4 ;
create unique index image_stage_primary_key_index 
    on image_stage (imgstg_img_zdb_id,imgstg_start_stg_zdb_id,
    imgstg_end_stg_zdb_id) using btree  in idxdbs4 ;
alter table image_stage add constraint primary 
    key (imgstg_img_zdb_id,imgstg_start_stg_zdb_id,imgstg_end_stg_zdb_id) 
    constraint image_stage_primary_key  ;
alter table image_stage add constraint (foreign 
    key (imgstg_img_zdb_id) references image 
     on delete cascade constraint imgstg_img_zdb_id_foregin_key);
    

alter table image_stage add constraint (foreign 
    key (imgstg_start_stg_zdb_id) references stage 
     constraint imgstg_start_stg_zdb_id_foregin_key);
    

alter table image_stage add constraint (foreign 
    key (imgstg_end_stg_zdb_id) references stage  
    constraint imgstg_end_stg_zdb_id_foregin_key);
    

create trigger image_stage_insert_trigger insert 
    on image_stage referencing new as new_stage
    
    for each row
        (
        execute procedure p_stg_hours_consistent(
		new_stage.imgstg_start_stg_zdb_id,
		new_stage.imgstg_end_stg_zdb_id ));

create trigger image_stage_update_trigger update 
    of imgstg_start_stg_zdb_id,imgstg_end_stg_zdb_id on 
    image_stage referencing new as new_stage
    for each row
        (
        execute procedure 
		p_stg_hours_consistent(new_stage.imgstg_start_stg_zdb_id,
		new_stage.imgstg_end_stg_zdb_id ));


create table marker_source (mrkrsrc_mrkr_zdb_id varchar(50) 
				not null constraint
				mrkrsrc_mrkr_lab_of_origin_not_null,
			    mrkrsrc_lab_of_origin_zdb_id varchar(50) 
				not null constraint 
				mrkrsrc_lab_of_origin_not_null
			    )
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32 next size 32 ;

create unique index marker_source_primary_key_index
  on marker_source (mrkrsrc_mrkr_zdb_id, mrkrsrc_lab_of_origin_zdb_id)
  using btree in idxdbs3 ;

create index mrkrsrc_mrkr_zdb_id_foreign_key_index
  on marker_Source (mrkrsrc_mrkr_zdb_id)
  using btree in idxdbs3 ;

create index mrkrsrc_lab_of_origin_zdb_id_foreign_key_index
  on marker_source (mrkrsrc_lab_of_origin_zdb_id)
  using btree in idxdbs3 ;

alter table marker_source
  add constraint primary key (mrkrsrc_mrkr_zdb_id, 
	mrkrsrc_lab_of_origin_zdb_id)
  constraint marker_source_primary_key;

alter table marker_source
  add constraint (foreign key (mrkrsrc_mrkr_zdb_id)
  references marker on delete cascade constraint
  mrkrsrc_mrkr_zdb_id_foreign_key_odc);

alter table marker_source
  add constraint (foreign key (mrkrsrc_lab_of_origin_zdb_id)
  references zdb_active_source on delete cascade constraint
  mrkrsrc_lab_of_origin_zdb_id_foreign_key_odc);

commit work ;
--rollback work ;