begin work ;

create index fig_label_index 
 on figure (fig_label)
  using btree in idxdbs3 ;

alter table marker
  drop constraint mrkr_abbrev_unique ;

drop index mrkr_abbrev_index ;


alter table marker
  modify (mrkr_abbrev varchar(60) not null constraint
	mrkr_abbrev_not_null) ;

create unique index mrkr_abbrev_index on
    marker (mrkr_abbrev) using btree in idxdbs2 ;


alter table marker 
  add constraint unique (mrkr_abbrev) 
    constraint mrkr_abbrev_unique  ;


--still need:
create table feature_type (
	ftrtype_name	        varchar(30)
	  not null constraint ftrtype_name_not_null ,
	ftrtype_significance	integer,
	ftrtype_type_display	varchar(30)
	)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 32 next size 32 lock mode row ;

--drop trigger locus_update_trigger ;
--drop trigger locusreg_name_update_trigger ;
--drop trigger locusreg_abbrev_update_trigger ;

set constraints all deferred ;

update locus
  set locus_name = replace(locus_name, ")",":")
   where locus_name like 'T(%'
   or locus_name like 'Df%';

update locus
  set locus_name = locus_name||")"
  where locus_name like 'T(%'
   or locus_name like 'Df%';

update locus
  set abbrev = replace(abbrev, ")",":")
   where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus
  set abbrev = abbrev||")"
  where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus
  set abbrev = replace(abbrev, ":)",")")
   where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus
  set locus_name = replace(locus_name, ":)",")")
   where locus_name like 'T(%'
   or locus_name like 'Df%';

---

update locus_registration
  set locus_name = replace(locus_name, ")",":")
   where locus_name like 'T(%'
   or locus_name like 'Df%';

update locus_registration
  set locus_name = locus_name||")"
  where locus_name like 'T(%'
   or locus_name like 'Df%';

update locus_registration
  set abbrev = replace(abbrev, ")",":")
   where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus_registration
  set abbrev = abbrev||")"
  where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus_registration
  set abbrev = replace(abbrev, ":)",")")
   where abbrev like 'T(%'
   or abbrev like 'Df%';

update locus_registration
  set locus_name = replace(locus_name, ":)",")")
   where locus_name like 'T(%'
   or locus_name like 'Df%';

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('SEQUENCE_VARIANT', '9', 'unknown') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('UNSPECIFIED', '9', 'unspecified') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('POINT_MUTATION', '1','Point Mutation') ;

--insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
--  values ('TRANSVERSION', '26','Transversion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('INSERTION', '2','Insertion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('DEFICIENCY', '5','Deficiency') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('TRANSLOC', '4','Translocation') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('INVERSION', '4','Inversion') ;

--Tg nomenclature redo case

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('TRANSGENIC_INSERTION', '3','Transgenic Insertion') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('LOCUS', '25', 'LOCUS') ;

insert into feature_type (ftrtype_name, ftrtype_significance, ftrtype_type_display)
  values ('ALT', '25', 'Alelle') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('LOCUS', '17','LOCUS') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('TGCONSTRCT', '18', 'Transgenic Construct') ;

--insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
--  values ('TRANSLOC', '18', 'Translocation') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('GTCONSTRCT', '19', 'Gene Trap Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('PTCONSTRCT', '20', 'Promoter Trap Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('ETCONSTRCT', '21', 'Enhancer Trap Construct') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('REGION', '22','Other Feature') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('GENEFAMILY', '23','Gene Family') ;

insert into marker_types (marker_type, mrkrtype_significance, mrkrtype_type_display)
  values ('EFG', '23','Engineered Foreign Gene') ;

update marker_types 
  set mrkrtype_type_display = 'BAC END'
  where mrkrtype_type_display = 'BAC_END' ;

update marker_types
  set mrkrtype_type_display = 'PAC END'
  where mrkrtype_type_display = 'PAC_END';

!echo "EFG zdb_object_type" ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('EFG', '11/15/2005','1','','marker', 
	  'mrkr_zdb_id', 't','f', '2') ;


--new marker relationships for TG, ET, PT, and GT constructs

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('CONSTRUCT', 'this group contains genetically engineered constructs; promoter, enhancer, and gene traps, as well as trangenic construcs');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('REGION', 'this group contains genetic components like IRES; also refferd to as "special markers."');

insert into marker_type_group (mtgrp_name, mtgrp_comments)
  values ('GENEDOM_AND_EFG', 'this group contains genes, psuedogenes and engineered foreign genes (EFGs)');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('TGCONSTRCT', 'CONSTRUCT');

--insert into marker_type_group_member (mtgrpmem_mrkr_type, 
--			mtgrpmem_mrkr_type_group)
--  values ('TRANSLOC', 'CONSTRUCT');

--insert into marker_type_group_member (mtgrpmem_mrkr_type, 
--			mtgrpmem_mrkr_type_group)
--  values ('GENE', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('LOCUS', 'CONSTRUCT');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('EFG', 'GENEDOM_AND_EFG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GENE', 'GENEDOM_AND_EFG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GENEP', 'GENEDOM_AND_EFG');

insert into marker_type_group_member (mtgrpmem_mrkr_type, 
			mtgrpmem_mrkr_type_group)
  values ('GENEFAMILY', 'GENEDOM_AND_EFG');


--insert into marker_type_group_member (mtgrpmem_mrkr_type, 
--			mtgrpmem_mrkr_type_group)
--  values ('GENEP', 'CONSTRUCT');

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

--insert into marker_type_group_member (mtgrpmem_mrkr_type, 
--			mtgrpmem_mrkr_type_group)
--  values ('TRANSLOC', 'SEARCH_MKSEG');

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
 values ('promoter of',
	 'CONSTRUCT',
	 'GENEDOM_AND_EFG',
	 'has promoter',
	 'is promoter of');

insert into marker_relationship_type (mreltype_name, 
	mreltype_mrkr_type_group_1,
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
 values ('coding sequence of',
	 'CONSTRUCT',
	 'GENEDOM_AND_EFG',
	 'has coding sequence',
	 'is coding sequence of');

insert into marker_relationship_type (mreltype_name, 
	mreltype_mrkr_type_group_1,
	mreltype_mrkr_type_group_2,
	mreltype_1_to_2_comments,
	mreltype_2_to_1_comments)
 values ('contains special feature',
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

--case 1489

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('MUTANT', 
	 'all valid allele types in this group');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('INSERTION', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('POINT_MUTATION', 
	 'group containing only this feature type');

--insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
--  values ('TRANSVERSION', 
--	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('INVERSION', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('DEFICIENCY', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('TRANSLOC', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('TG_INSERTION', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('SEQUENCE_VARIANT', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('UNSPECIFIED', 
	 'group containing only this feature type');

insert into feature_type_group (ftrgrp_name, ftrgrp_comments)
  values ('ALL_INSERTIONS', 
	 'group containing insertions and transgenic_insertion feature types');


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

create index feature_type_group_member_group_foreign_key_index
  on feature_type_group_member (ftrgrpmem_ftr_type_group)
  using btree in idxdbs4 ;

alter table feature_type_group_member 
  add constraint primary key (ftrgrpmem_ftr_type, 
  ftrgrpmem_ftr_type_group) constraint
  feature_type_group_member_primary_key ;

alter table feature_type_group_member 
  add constraint (foreign key (ftrgrpmem_ftr_type)
  references feature_type constraint
  feature_type_group_member_type_foreign_key) ;

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
  values ('POINT_MUTATION','POINT_MUTATION') ;

--insert into feature_type_group_member (ftrgrpmem_ftr_type,
--		ftrgrpmem_ftr_type_group)
--  values ('TRANSVERSION','MUTANT') ;

--insert into feature_type_group_member (ftrgrpmem_ftr_type,
--		ftrgrpmem_ftr_type_group)
--  values ('TRANSVERSION','TRANSVERSION') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INSERTION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INSERTION','INSERTION') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('UNSPECIFIED','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('UNSPECIFIED','UNSPECIFIED') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('DEFICIENCY','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('DEFICIENCY','DEFICIENCY') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSLOC','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSLOC','TRANSLOC') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INVERSION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INVERSION','INVERSION') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSGENIC_INSERTION','MUTANT') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSGENIC_INSERTION','TG_INSERTION') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('TRANSGENIC_INSERTION','ALL_INSERTIONS') ;

insert into feature_type_group_member (ftrgrpmem_ftr_type,
		ftrgrpmem_ftr_type_group)
  values ('INSERTION','ALL_INSERTIONS') ;

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


create index feature_marker_relationship_type_ftr_group_index
  on feature_marker_relationship_type (fmreltype_ftr_type_group)
  using btree in idxdbs2 ;

create index feature_marker_relationship_type_mrkr_group_index
  on feature_marker_relationship_type (fmreltype_mrkr_type_group)
  using btree in idxdbs2 ;

create unique index feature_marker_relationship_type_primary_key_index
  on feature_marker_relationship_type (fmreltype_name)
  using btree
  in idxdbs2 ;

alter table feature_marker_relationship_type
  add constraint primary key (fmreltype_name)
  constraint feature_marker_relationship_type_primary_key ;


insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('contains sequence feature', 'MUTANT','CONSTRUCT','Contains','Contained in');

insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('is allele of', 'MUTANT','GENEDOM','Is Allele Of','Has Allele');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('LOCUS', 'MUTANT');

insert into marker_type_group_member (mtgrpmem_mrkr_type,
    mtgrpmem_mrkr_type_group)
  values ('TGCONSTRCT', 'MUTANT');

--insert into marker_type_group_member (mtgrpmem_mrkr_type,
--    mtgrpmem_mrkr_type_group)
--  values ('TRANSLOC', 'MUTANT');

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
	feature_abbrev		 varchar(70) 	
		not null constraint feature_abbrev_not_null,
        feature_type		 varchar(30) 	
		not null constraint feature_type_not_null,
	feature_lab_of_origin    varchar(50),
	feature_comments	 lvarchar,
	feature_name_order 	 varchar(255)	
		not null constraint feature_name_order_not_null,
	feature_abbrev_order	 varchar(255)
		not null constraint feature_abrv_order_not_null,
	feature_date_entered	 datetime year to second
		default current year to second
	)

fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 9664 next size 9664 lock mode row ;

create unique index feature_primary_key_index 
  on feature (feature_zdb_id) using btree 
  in idxdbs3 ;

create index feature_lab_of_origin_foreign_key_index 
  on feature (feature_lab_of_origin) using btree
  in idxdbs3 ;


create unique index feature_abbrev_alternate_key 
  on feature (feature_abbrev)
  using btree in idxdbs2 ;

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

--create table genotype

create table genotype (geno_zdb_id varchar(50) not null constraint 
				geno_zdb_id_not_null,
			geno_display_name varchar(255) not null constraint 
				geno_name_not_null,
			geno_handle varchar(255) not null constraint 
				geno_abbrev_not_null,
			geno_supplier_stock_number varchar(100),
			geno_date_entered datetime year to fraction(3) default 
				current year to fraction(3),
			geno_name_order varchar(50),
			geno_is_wildtype boolean default 'f',
			geno_is_extinct boolean default 'f'
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

create trigger genotype_insert_trigger insert on genotype
  referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_display_name ) 
    into genotype.geno_display_name,
        execute function scrub_char(new_genotype.geno_handle ) 
    into genotype.geno_handle,
        execute function zero_pad(new_genotype.geno_display_name ) 
    into genotype.geno_name_order
);

create trigger geno_display_name_update_trigger update of geno_display_name
  on genotype referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_display_name ) 
    into genotype.geno_display_name,
        execute function zero_pad(new_genotype.geno_display_name ) 
    into genotype.geno_name_order
);

create trigger geno_handle_update_trigger update of geno_handle
  on genotype referencing new as new_genotype
    for each row
    (
        execute function scrub_char(new_genotype.geno_handle ) 
    into genotype.geno_handle
);

--reorder in response to case 1431, 1425

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
	into feature.feature_abbrev_order,
	 execute procedure p_update_related_genotype_names(newf.feature_zdb_id)
);

create trigger feature_name_update_trigger update of 
    feature_name on feature referencing old as oldf new 
    as newf
      for each row
          (execute function zero_pad(newf.feature_name ) 
      into feature.feature_name_order,
         execute procedure fhist_event(newf.feature_zdb_id,
		'renamed',
		newf.feature_name ,oldf.feature_name),
	 execute procedure p_update_related_genotype_names(newf.feature_zdb_id)
);


insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('GENO', '1/5/2007','1','','genotype', 
	  'geno_zdb_id', 't','f', '2') ;


--create table zygocity (cv table)

create table zygocity (zyg_zdb_id varchar(50) not null constraint
			zygocity_zdb_id_not_null,
		       zyg_name  varchar(60) not null constraint
			zygocity_name_not_null,
		       zyg_abbrev varchar(10),
		       zyg_definition varchar(255) not null constraint
			zygocity_definition_not_null,
		       zyg_allele_display varchar(10),
		       zyg_gene_prefix varchar(10))
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

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev, 
			zyg_definition, 
			zyg_allele_display)
  values (get_id('ZYG'), 	
		'homozygous', 
		'2',
		'two copies of the allele at a given locus',
		"/allele");

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev, 
			zyg_definition,
			zyg_allele_display)
  values (get_id('ZYG'), 
		'heterozygous', 
		'1',
		'one copy of the allele at a given locus',
		'/+');

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev,
			zyg_definition,
			zyg_allele_display)
  values (get_id('ZYG'), 
		'hemizygous',
		'I',
		'insertional construct on one chromosome only: The state of a gene present in only one copy in a diploid cell, such as a gene on the X chromosome in a male mammal, or a gene whose homologue has been deleted',
		'/0');

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev,
			zyg_definition,
			zyg_gene_prefix)
  values (get_id('ZYG'), 
		'maternal zygotic', 
		'MZ',
		'mom and self homozygous',
		'MZ');

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev,
			zyg_definition,
			zyg_gene_prefix)
  values (get_id('ZYG'), 
		'paternal zygotic',
		'PZ', 
		'dad and self homozygous',
		'PZ');

insert into zygocity (zyg_zdb_id, 
			zyg_name, zyg_abbrev,
			zyg_definition)
  values (get_id('ZYG'), 
		'complex', 
		'C',
		'genotype with multiple insertions of insertional construct');

insert into zygocity (zyg_zdb_id, 
			zyg_name,
			zyg_abbrev, 
			zyg_definition)
  values (get_id('ZYG'), 
		'unknown', 
		'U',
		'zygocity unknown');

insert into zygocity (zyg_zdb_id, 
			zyg_name, 
			zyg_abbrev,
			zyg_definition)
  values (get_id('ZYG'), 
		'wild type', 
		'W',
		'parental zygocity is non-mutant');

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

create index genofeat_feature_zdb_id_foreign_key_index
  on genotype_feature (genofeat_feature_zdb_id)
  using btree in idxdbs3 ;

create index genofeat_chromosome_foreign_key_index
  on genotype_feature (genofeat_chromosome)
  using btree in idxdbs3 ;

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

--! ODC from genotype to genotype_feature...took this out b/c of 
--frodo meeting on 6/7

--took out odc from active data too.

alter table genotype_feature
   add constraint (foreign key (genofeat_zdb_id)
   references zdb_active_data constraint
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

create index genotype_genotype_zdb_id_foreign_key_index
  on genotype_background (genoback_geno_zdb_id)
  using btree in idxdbs3 ;

create index genoback_background_zdb_id_foreign_key_index
  on genotype_background (genoback_background_zdb_id)
  using btree in idxdbs3 ;

create unique index genotype_background_primary_key_index
  on genotype_background (genoback_geno_zdb_id, 
				genoback_background_zdb_id)
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
  values ('quality', '2');

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

create index genotype_experiment_exp_zdb_id_foreign_key_index
 on genotype_experiment (genox_exp_zdb_id)
  using btree in idxdbs3 ;

create index genotype_experiment_geno_zdb_id_foreign_key_index
 on genotype_experiment (genox_geno_zdb_id)
  using btree in idxdbs3 ;

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


--create the apato tag tale


create table apato_tag (apatotag_name varchar(25) not null constraint
				apatotag_name_not_null)
in tbldbs2 extent size 8 next size 8 lock mode row ;

create unique index apatotag_name_primary_key_index
  on apato_tag (apatotag_name)
  using btree in idxdbs2 ;

alter table apato_tag
  add constraint primary key (apatotag_name)
  constraint apatotag_primary_key ;

insert into apato_tag
  values ('abnormal') ;

insert into apato_tag
  values ('normal') ;

insert into apato_tag
  values ('abnormal/present') ;

insert into apato_tag
  values ('abnormal/absent') ;

insert into apato_tag
  values ('normal/present') ;

insert into apato_tag
  values ('normal/absent') ;

create table apato_infrastructure (api_zdb_id varchar(50) not null constraint
						api_zdb_id_not_null,
					api_entity_a_zdb_id varchar(50) 
						not null constraint
						api_entity_a_zdb_id_not_null,
					api_entity_b_zdb_id varchar(50),
					api_quality_zdb_id varchar(50)
						not null constraint
						api_quality_zdb_id_not_null,
					api_tag varchar(25)
						not null constraint
						api_tag_not_null,
					api_pub_zdb_id varchar(50)
						not null constraint
						api_pub_zdb_id_not_null,
					api_curator_zdb_id varchar(50)
						not null constraint
						api_curator_zdb_id_not_null,
					api_date datetime year to day
						default current year to day
						not null constraint
						api_date_not_null)
in tbldbs3 extent size 32 next size 32 lock mode page ;

create unique index api_zdb_id_primary_key_index
  on apato_infrastructure (api_zdb_id)
  using btree in idxdbs4 ;

create index api_entity_a_zdb_id_fk_index
  on apato_infrastructure (api_entity_a_zdb_id)
  using btree in idxdbs4 ;

create index api_entity_b_zdb_id_fk_index
  on apato_infrastructure (api_entity_b_zdb_id)
  using btree in idxdbs4 ;

create index api_tag_zdb_id_fk_index
  on apato_infrastructure (api_tag)
  using btree in idxdbs4 ;

create index api_curator_zdb_id_fk_index
  on apato_infrastructure (api_curator_zdb_id)
  using btree in idxdbs4 ;

create index api_pub_zdb_id_fk_index
  on apato_infrastructure (api_pub_zdb_id)
  using btree in idxdbs4 ;

create index api_quality_zdb_id_fk_index
  on apato_infrastructure (api_quality_zdb_id)
  using btree in idxdbs4 ;

alter table apato_infrastructure
  add constraint primary key (api_zdb_id)
  constraint apato_infrastructure_primary_key ;

alter table apato_infrastructure
  add constraint (foreign key (api_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_zdb_id_active_data_fk_odc) ;

alter table apato_infrastructure
  add constraint (foreign key (api_entity_a_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_a_zdb_id_fk_odc) ;

alter table apato_infrastructure
  add constraint (foreign key (api_entity_b_zdb_id)  
  references zdb_active_data on delete cascade constraint
  apato_entity_b_zdb_id_fk_odc) ;

alter table apato_infrastructure
  add constraint (foreign key (api_tag)
  references apato_tag constraint
  apato_tag_fk) ;

alter table apato_infrastructure
  add constraint (foreign key (api_quality_zdb_id)
  references term constraint
  apato_quality_zdb_id_fk) ;

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('API', '07/26/2006','1','','apato_infrastructure', 
	  'api_zdb_id', 't','f', '2') ;

--create atomic_phenotype table

create table atomic_phenotype (apato_zdb_id varchar(50) not null
					constraint pato_zdb_id_not_null,
				apato_genox_zdb_id varchar(50) not null
					constraint apato_genox_zdb_id_not_null,
				apato_start_stg_zdb_id varchar(50),
				apato_end_stg_zdb_id varchar(50),
				apato_pub_zdb_id varchar(50) 
					not null constraint
					apato_pub_zdb_id_not_null,
				apato_entity_a_zdb_id varchar(50) 
					not null constraint 
					apato_entity_a_zdb_id_not_null,
				apato_entity_b_zdb_id varchar(50),
				apato_quality_zdb_id varchar(50) 
					default 'quality'
					not null constraint apato_quality_not_null ,
				apato_tag varchar(25) default 'abnormal'
					not null constraint apato_tag_not_null
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 1024 next size 1024 
 lock mode page;

create unique index atomic_phenotype_primary_key_index
  on atomic_phenotype (apato_zdb_id)
  using btree in idxdbs2 ;

create index apato_genox_fk_index 
  on atomic_phenotype (apato_genox_zdb_id)
  using btree in idxdbs2 ;

create index apato_start_stg_fk_index 
  on atomic_phenotype (apato_start_stg_zdb_id)
  using btree in idxdbs2 ;

create index apato_end_stg_fk_index
  on atomic_phenotype (apato_end_stg_zdb_id)
  using btree in idxdbs2 ;

create index apato_entity_a_zdb_id_fk_index
  on atomic_phenotype (apato_entity_a_zdb_id)
  using btree in idxdbs2 ;

create index apato_entity_b_zdb_id_fk_index
  on atomic_phenotype (apato_entity_b_zdb_id)
  using btree in idxdbs2 ;

create index apato_quality_zdb_id_fk_index
  on atomic_phenotype (apato_quality_zdb_id)
  using btree in idxdbs2;

create index apato_tag_fk_index
  on atomic_phenotype (apato_tag)
  using btree in idxdbs2;

create unique index atomic_phenotype_alternate_key_index
  on atomic_phenotype (apato_genox_zdb_id,
			apato_pub_zdb_id, 
			apato_start_stg_zdb_id, 
			apato_end_stg_zdb_id,
			apato_entity_a_zdb_id,
			apato_entity_b_zdb_id,
			apato_quality_zdb_id,
			apato_tag)
  using btree in idxdbs3;

alter table atomic_phenotype
  add constraint primary key (apato_zdb_id)
  constraint atomic_phenotype_primary_key ;

alter table atomic_phenotype
  add constraint (foreign key (apato_zdb_id)
  references zdb_active_data on delete cascade
  constraint apato_zdb_active_data_foreign_key_odc);

alter table atomic_phenotype
  add constraint unique (apato_genox_zdb_id, 
			 apato_pub_zdb_id,
			 apato_start_stg_zdb_id, 
			 apato_end_stg_zdb_id,
			 apato_entity_a_zdb_id,
			 apato_entity_b_zdb_id,
			 apato_quality_zdb_id,
			 apato_tag)
  constraint atomic_phenotype_alternate_key_constraint ;

alter table atomic_phenotype
  add constraint (foreign key (apato_genox_zdb_id)
  references genotype_experiment on delete cascade
  constraint apato_genox_foreign_key_odc);

alter table atomic_phenotype
  add constraint (foreign key (apato_start_stg_zdb_id)
  references stage
  constraint apato_start_stg_foreign_key);

alter table atomic_phenotype
  add constraint (foreign key (apato_end_stg_zdb_id)
  references stage
  constraint apato_end_stage_foreign_key);

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_a_zdb_id)
  references zdb_active_data
  on delete cascade constraint apato_entity_a_foreign_key);

alter table atomic_phenotype
  add constraint (foreign key (apato_entity_b_zdb_id)
  references zdb_active_data
  on delete cascade constraint apato_entity_b_foreign_key);

alter table atomic_phenotype
  add constraint (foreign key (apato_quality_zdb_id)
  references term
  constraint apato_quality_foreign_key);

alter table atomic_phenotype
  add constraint (foreign key (apato_tag)
  references apato_tag
  constraint apato_tag_foreign_key);

insert into zdb_object_type (zobjtype_name, zobjtype_day, 
	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
	zobjtype_home_zdb_id_column,
	zobjtype_is_data, zobjtype_is_source, 
	zobjtype_attribution_display_tier)
  values ('APATO', '11/15/2005','1','','atomic_phenotype', 
	  'apato_zdb_id', 't','f', '2') ;

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

select count(*), ftrgrp_name
  from feature_type_group
  group by ftrgrp_name
  having count(*) > 1;

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

--insert into zdb_object_type (zobjtype_name, zobjtype_day, 
--	zobjtype_seq, zobjtype_app_page, zobjtype_home_table, 
--	zobjtype_home_zdb_id_column,
--	zobjtype_is_data, zobjtype_is_source, 
--	zobjtype_attribution_display_tier)
--  values ('TRANSLOC', '11/15/2005','1','','marker', 
--	  'mrkr_zdb_id', 't','f', '2') ;


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

--create table pato_figure 

create table apato_figure (apatofig_apato_zdb_id varchar(50) 
				not null constraint 
				apatofig_pato_zdb_id_not_null,
			    apatofig_fig_zdb_id varchar(50)
				not null constraint 
				apatofig_fig_zdb_id_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 1024 next size 1024 lock mode row;

create index apatofig_fig_foreign_key_index
  on apato_figure (apatofig_fig_zdb_id) 
  using btree in idxdbs4 ;

create index apatofig_pato_zdb_id_foreign_key_index
  on apato_figure (apatofig_apato_zdb_id)
  using btree in idxdbs4 ;

create unique index apatofig_primary_key_index
  on apato_figure (apatofig_apato_zdb_id, apatofig_fig_zdb_id)
  using btree in idxdbs4 ;

alter table apato_figure
  add constraint primary key (apatofig_apato_zdb_id, apatofig_fig_zdb_id)
  constraint apato_figure_primary_key ;

alter table apato_figure
  add constraint (foreign key (apatofig_apato_zdb_id)
  references zdb_active_data on delete cascade
  constraint apatofig_pato_foreign_key_odc) ;

alter table apato_figure
  add constraint (foreign key (apatofig_fig_zdb_id)
  references figure on delete cascade
  constraint apatofig_fig_foreign_key_odc) ;

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

create unique index fish_id_index
  on tmp_fish_image (fish_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_fish_image ;

set constraints all deferred ;

alter table figure
  modify (fig_label varchar(50)) ;

create temp table tmp_note (zdb_id varchar(50),
				note lvarchar(2000))
with no log ;

load from CommentsTabbed
  insert into tmp_note;

insert into figure (fig_zdb_id,
			fig_label, fig_comments, fig_source_zdb_id)
select get_id('FIG'),
	fish_id,
	fish_id,
        'ZDB-PUB-060503-2'
  from tmp_fish_image;

update statistics high for table figure ;

--more figures for phenotype entries with no photos.
!echo "this is the new figure creation for pato annotations with no images" ;


update statistics high for table figure ;

insert into figure (fig_zdb_id, fig_label, fig_comments, fig_source_zdb_id)
  select get_id('FIG'),
	fish.zdb_id,
	fish.zdb_id,
	'ZDB-PUB-060503-2'
    from fish
    where not exists (select 'x' 
			from figure b
			where b.fig_label = fish.zdb_id)   
	and fish.zdb_id not like 'ZDB-FISH-060608-%'
   	and fish.zdb_id not like 'ZDB-FISH-061101-%';

!echo "CASE 1440" ;

insert into figure (fig_zdb_id, fig_label, fig_comments, fig_source_zdb_id)
  select get_id('FIG'),
	fish.zdb_id,
	fish.zdb_id,
	'ZDB-PUB-060606-1'
    from fish
    where not exists (select 'x' 
			from figure b
			where b.fig_label = fish.zdb_id) 
   and (fish.zdb_id like 'ZDB-FISH-060608-%'
   	or fish.zdb_id like 'ZDB-FISH-061101-%');


update figure
  set fig_caption = (select "<br><b>Original Submitter Comments: </b>"||note
  			from tmp_note
			where fig_label = zdb_id)
  where exists (Select 'x'
		from tmp_note
		where zdb_id = fig_label);

update statistics high for table figure ;

--update figure
--  set fig_label = (select "Fig. for ("||fish.allele||")"
--			from fish
--			where fig_label = fish.zdb_id)
--  where fig_label like 'ZDB-FISH-%' ;

!echo "fish_image_fig update";

update fish_image
  set fimg_fig_zdb_id = (select fig_zdb_id
				from figure
				where fimg_fish_zdb_id = fig_label)
  where fimg_fig_zdb_id is null;

select fimg_Fig_zdb_id
  from fish_image
  where fimg_fish_zdb_id ='ZDB-FISH-040726-1';

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

select ftrgrpmem_ftr_type_group
  from feature_type_group_member
  where not exists (Select 'x'
			from feature_type_group
			where ftrgrpmem_ftr_type_group = ftrgrp_name);

alter table feature_type_group_member add constraint 
    (foreign key (ftrgrpmem_ftr_type_group) references
	feature_type_group constraint ftrgrpmem_ftr_type_group_foreign_key);

commit work ;
--rollback work ;