begin work ;

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
  select feature_zdb_id, feature_lab_of_origin 
    from feature
    where feature_lab_of_origin is not null;

insert into int_data_source (ids_data_zdb_id, ids_source_zdb_id)
	select zrepld_new_zdb_id, lab
          from fish, zdb_replaced_data
          where fish.zdb_id = zrepld_old_zdb_id
	  and zrepld_new_zdb_id like 'ZDB-GENO-%'
	  and line_type = 'wild type'
	  and lab is not null;


alter table feature
  drop feature_lab_of_origin ;

insert into curation_topic (curtopic_name)
  values ('Genotype') ;

insert into curation_topic (curtopic_name)
  values ('Transgenic Construct') ;

set constraints all deferred ;

update curation
  set cur_topic = "Features (Mutant)"
  where cur_topic = "Mutant" ;

update curation_topic
  set curtopic_name = "Features (Mutant)"
  where curtopic_name = "Mutant" ;

update publication_claim
  set pubcl_topic = "Features (Mutant)"
  where pubcl_topic = "Mutant" ;

delete from marker_types
 where marker_type = 'LOCUS';

delete from marker_types
 where marker_type = 'ALT';

delete from feature_type
  where ftrtype_name = 'LOCUS';

delete from feature_type
  where ftrtype_name = 'ALT';

delete from marker_type_group_member
  where mtgrpmem_mrkr_Type = 'LOCUS' ;

delete from feature_type_group_member
  where ftrgrpmem_ftr_Type = 'LOCUS' ;

delete from feature_type_group_member
  where ftrgrpmem_ftr_Type = 'ALT' ;

delete from marker_type_group_member
  where mtgrpmem_mrkr_type = 'ALT' ;

set constraints all immediate ;

create temp table tmp_seg (fish_id varchar(50), source_id varchar(50), disp varchar(50), type char(30))
with no log ;

load from segregation.unl 
  insert into tmp_seg ;

select first 1 * from genotype ;


create temp table tmp_seg_convert (fish_id varchar(50), source_id varchar(50), type char(30),convert_id varchar(50),
  feature_id varchar(50), marker_id varchar(50))
with no log ;

insert into tmp_seg_convert (fish_id, source_id, type, convert_id)
  select fish_id, source_id, type, zrepld_new_zdb_id
    from tmp_seg, zdb_replaced_data
    where fish_id = zrepld_old_zdb_id ;

update tmp_seg_convert
  set feature_id = (Select genofeat_feature_zdb_id
                      from genotype_feature
			where genofeat_geno_zdb_id = convert_id);

update tmp_Seg_convert
  set marker_id = (select fmrel_mrkr_zdb_id
			from feature_markeR_relationship
			where fmrel_ftr_zdb_id = feature_id);

!echo "number with marker comments" ;

select first 1 * from tmp_seg_convert
  where exists (select 'x'
		  from marker
		  where mrkr_comments is not null
		  and mrkr_zdb_id = marker_id);

!echo "number with external commentS" ;

select first 1 * from external_note
  where extnote_data_zdb_id like 'ZDB-GENOX-%' ;


select count(*) from tmp_seg_convert
  where exists (select 'x' from external_note, genotype_experiment
		  where extnote_data_zdb_id = genox_zdb_id
		  and genox_geno_zdb_id = convert_id);

!echo "number with feature comments" 

select count(*) from tmp_seg_convert 
  where exists (select 'x'	
		  from feature
		  where feature_comments is not null
		  and feature_zdb_id = feature_id);


unload to segregation
  select recattrib_datA_zdb_id, recattrib_source_zdb_id
    from record_attribution
    where recattrib_source_type = 'segregation' ;



commit work ;

--rollback work ;