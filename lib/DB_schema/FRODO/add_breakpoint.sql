begin work ;

update statistics for procedure ;

alter table genotype_feature
  drop constraint genotype_feature_alternate_key;

drop index genotype_Feature_alternate_key_index ;

delete from mapped_deletion
  where allele = 'b333'
  and marker_id = 'ZDB-GENE-031118-202' ;


set constraints all deferred ;

create temp table tmp_fmrel (fm_id varchar(50))
with no log ;

insert into tmp_fmrel 
 select get_id('FMREL')
   from single ;

insert into feature_marker_relationship (fmrel_zdb_id,
		fmrel_ftr_zdb_id,
		fmrel_mrkr_zdb_id,
		fmrel_type)
  select fm_id,
	(select feature_zdb_id 	
	   from feature
	   where feature_name = 'Df(LG08:tbx16)b333'),
	'ZDB-GENE-031118-202',
	'is allele of'
    from tmp_fmrel ;


insert into zdb_active_data
  select fm_id
    from tmp_fmrel;


insert into record_Attribution (recattrib_datA_zdb_id,
					recattrib_source_zdb_id,
					recattrib_source_type)
  select fm_id,
	'ZDB-PUB-060313-13',
	'standard'
     from tmp_fmrel 
     where not exists (Select 'x'
			from record_attribution b
			where b.recattrib_data_zdb_id = fm_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-060313-13' 
			and b.recattrib_source_type = 'standard');


update marker
  set mrkr_abbrev = substr(mrkr_abbrev,0,3)||"(l)"
  where mrkr_abbrev like '%zdb-locus-%';

update marker
  set mrkr_name = mrkr_name||"(locus)"
  where mrkr_abbrev like '%(l)';

insert into data_alias (dalias_zdb_id, dalias_alias, dalias_group,
			dalias_data_zdb_id)
  select get_id ('DALIAS'), substr(mrkr_abbrev,0,3),
	'alias',mrkr_Zdb_id
    from marker
    where mrkr_abbrev like '% locus%'
  and not exists (select 'x'
			from data_alias,marker
   			where dalias_data_zdb_id = mrkr_Zdb_id
			and dalias_alias = substr(mrkr_abbrev,0,3)
			and mrkr_abbrev like '% locus%');

insert into zdb_active_data
  select dalias_zdb_id
    from data_alias
    where not exists (Select 'x'
			from zdb_active_Data
			where dalias_zdb_id =zactvd_zdb_id);

---cases: 1094 1354 and the one about Tg PATO annotations.

update atomic_phenotype
  set (apato_quality_zdb_id, apato_tag) = ((select term_Zdb_id
				from term
				where term_ont_id = 'PATO:0000060'), 'present')
   where exists (select 'x'
		   from genotype_Experiment, genotype, genotype_feature, 
			feature
		   where genox_zdb_id = apato_genox_zdb_id
			and genox_geno_zdb_id = geno_zdb_id
			and geno_zdb_id = genofeat_geno_zdb_id
			and feature_zdb_id = genofeat_feature_zdb_id
			and feature_type = 'INSERTION'
			and feature_name like 'Tg(%'
			and geno_display_name like 'Tg(%'
			and feature_name not like 'Tg(mitfa:BRAF-V600E)%');


select count(*), apato_entity_a_zdb_id, apato_quality_zdb_id,
		apato_start_Stg_zdb_id, apato_end_stg_zdb_id,
		apato_tag, apato_genox_zdb_id
  from atomic_phenotype
  group by apato_entity_a_zdb_id, apato_quality_zdb_id,
		apato_start_Stg_zdb_id, apato_end_stg_zdb_id,
		apato_tag, apato_genox_zdb_id
  having count(*) > 1;

select * from genotype, genotype_experiment
  where geno_zdb_id = genox_geno_zdb_id
  and (genox_zdb_id = 'ZDB-GENOX-061117-30'
		or genox_zdb_id = 'ZDB-GENOX-061117-530');

delete from zdb_active_data
  where exists (Select 'x'
		  from datA_alias, marker_history, marker
		  where mhist_dalias_zdb_id = dalias_zdb_id
		  and mrkr_zdb_id = dalias_data_zdb_id
		  and dalias_alias = mrkr_abbrev
		  and mrkr_type = 'GENE'
		  and mhist_mrkr_zdb_id = mrkr_zdb_id
		and mhist_zdb_id = zactvd_zdb_id);

delete from zdb_active_data
  where exists (Select 'x'
		  from datA_alias, marker
		  where mrkr_zdb_id = dalias_data_zdb_id
		  and dalias_alias = mrkr_abbrev
		  and mrkr_type = 'GENE'
		and dalias_zdb_id = zactvd_zdb_id);

delete from zdb_active_data
  where exists (Select 'x'
		  from datA_alias, marker_history, marker
		  where mhist_dalias_zdb_id = dalias_zdb_id
		  and dalias_alias like '%zdb-locus-%'
		  and mrkr_type = 'GENE'
		and mhist_zdb_id = zactvd_zdb_id);

delete from zdb_active_data
  where exists (Select 'x'
		  from datA_alias, marker
		  where mrkr_zdb_id = dalias_data_zdb_id
		  and dalias_alias like '%zdb-locus-%'
		  and mrkr_type = 'GENE'
		and dalias_zdb_id = zactvd_zdb_id);


select * from data_alias
  where dalias_alias = 'ovl';

update publication 
  set authors = 'Phenotype Annotation (1994-2006)'
  where zdb_id = 'ZDB-PUB-060503-2';

set constraints all immediate ;

alter table feature add constraint 
    (foreign key (feature_zdb_id) 
      references zdb_active_data on delete cascade constraint 
      feature_zactvd_zdb_id_foreign_key_odc)  ;


--case 1390
delete from zdb_active_data
  where exists (Select 'x'
		  from data_alias
		  where dalias_alias like 'un\_%'
		  and dalias_zdb_id = zactvd_zdb_id);


--case 1366

insert into int_data_supplier (idsup_data_zdb_id, 
				idsup_supplier_zdb_id,
				idsup_acc_num)
  select distinct genofeat_feature_zdb_id,
	b.idsup_supplier_zdb_id,
	b.idsup_acc_num
	from genotype_feature, int_data_supplier b
	where genofeat_geno_zdb_id = idsup_data_zdb_id ;


--case 1379

delete from genotype_feature
  where genofeat_geno_zdb_id in (
		'ZDB-GENO-060811-6',
		'ZDB-GENO-060811-7',
		'ZDB-GENO-060811-8',
		'ZDB-GENO-060811-9',
		'ZDB-GENO-060811-10',
		'ZDB-GENO-060811-11',
		'ZDB-GENO-060811-12',
		'ZDB-GENO-060811-12',
		'ZDB-GENO-060811-14',  
		'ZDB-GENO-060811-15') ;

delete from genotype_background
  where genoback_geno_zdb_id in (
		'ZDB-GENO-060811-6',
		'ZDB-GENO-060811-7',
		'ZDB-GENO-060811-8',
		'ZDB-GENO-060811-9',
		'ZDB-GENO-060811-10',
		'ZDB-GENO-060811-11',
		'ZDB-GENO-060811-12',
		'ZDB-GENO-060811-12',
		'ZDB-GENO-060811-14',  
		'ZDB-GENO-060811-15') ;

set constraints all deferred ;

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-6',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'fw07-g'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-7',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'r210'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );	
insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-8',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'sb55'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-9',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'b107'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-9',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'sb55'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown') );



insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-10',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm84'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );


insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-11',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm305'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-12',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tt250'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-13',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tz209'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-14',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm84'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );

insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-14',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm305'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'homozygous') );


insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-15',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm84'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'heterozygous') );


insert into genotype_feature (genofeat_zdb_id,
				genofeat_geno_zdb_id,
				genofeat_feature_zdb_id,
				genofeat_dad_zygocity,
				genofeat_mom_zygocity,
				genofeat_zygocity)
  values (get_id('GENOFEAT'),
		'ZDB-GENO-060811-15',
		(select feature_zdb_id
			from feature
			where feature_abbrev = 'tm305'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
	 	(select zyg_zdb_id
			from zygocity
			where zyg_name = 'unknown'),
		(select zyg_zdb_id
			from zygocity
			where zyg_name = 'heterozygous') );

update feature
  set feature_name = 'Tg(SS-hsp70l:dnPlexinA4-GFP)unspecified'
  where feature_name = 'Tg(SS-hsp70l:dnPlexinA4-GFP)Tg(SS-hsp70l:dnPlexinA4-GFP)unspecifi';

update feature
  set feature_abbrev = 'Tg(SS-hsp70l:dnPlexinA4-GFP)unspecified'
  where feature_abbrev= 'Tg(SS-hsp70l:dnPlexinA4-GFP)unspecifi';

update genotype
  set geno_display_name = 'Tg(SS-hsp70l:dnPlexinA4-GFP)unspecified'
  where geno_Zdb_id = 'ZDB-GENO-060825-2';


update genotype_feature
  set genofeat_mom_zygocity =
     (select zyg_zdb_id from zygocity
	where zyg_name = 'unknown')
  where genofeat_mom_zygocity is null;

update genotype_feature
  set genofeat_dad_zygocity =
     (select zyg_zdb_id from zygocity
	where zyg_name = 'unknown')
  where genofeat_dad_zygocity is null;

update genotype_feature
  set genofeat_zygocity =
     (select zyg_zdb_id from zygocity
	where zyg_name = 'unknown')
  where genofeat_zygocity is null;

create temp table tmp_genotype (genotype_id varchar(255), 
				genotype_handle varchar(255),
				genotype_display varchar(255))
with no log ;

insert into tmp_genotype
  select geno_zdb_id, 'test', 'test'
    from genotype 
    where geno_is_wildtype = 'f';

create unique index tg_index
  on tmp_genotype(genotype_id)
  using btree in idxdbs4; 

update statistics high for table tmp_genotype ;

update tmp_genotype
  set genotype_handle = get_genotype_handle(genotype_id) ;

select genotype_id, count(*)
  from tmp_genotype
  group by genotype_id
  having count(*) > 1;

update tmp_genotype
  set genotype_display = get_genotype_display(genotype_id);

unload to missing_handle_null
  select * from tmp_genotype
  where genotype_handle is null ;

unload to missing_handle_empty
  select * from tmp_genotype
  where genotype_handle = '' ;

delete from tmp_genotype
  where genotype_handle is null ;

delete from tmp_genotype
  where genotype_handle = '' ;

update genotype
  set geno_handle = (select genotype_handle
			from tmp_genotype
			where genotype_id = geno_zdb_id
                         and genotype_handle is not null 
			 and genotype_handle != '')
  where exists (select 'x'
		  from tmp_genotype
                  where geno_zdb_id = genotype_id);

update genotype
  set geno_display_name = (select genotype_display
			from tmp_genotype
			where genotype_id = geno_zdb_id
                         and genotype_display is not null 
			 and genotype_display != '')
  where exists (select 'x'
		  from tmp_genotype
                  where geno_zdb_id = genotype_id);


select count(*), geno_handle
  from genotype 
  group by geno_handle
  having count(*) > 1 ;

select count(*), geno_display_name
  from genotype 
  group by geno_display_name
  having count(*) > 1 ;

insert into zdb_active_data
  select genofeat_zdb_id
	from genotype_feature
	where not exists (Select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = genofeat_zdb_id);


set constraints all immediate ;

update statistics for procedure ;

alter table genotype
  drop constraint genotype_alternate_key ;

drop index
  genotype_alternate_key_index ;

update publication
  set authors = 'Phenotype Annotation (1994-2006)'
  where zdb_id = 'ZDB-PUB-060503-2' ;

--update feature
--  set feature_abbrev = feature_name
--  where feature_abbrev != feature_name ;

update marker
  set mrkr_abbrev = mrkr_name
  where mrkr_type = 'TGCONSTRCT' ;

update marker
 set mrkr_comments = null
  where mrkr_comments = 'NULL';

delete from external_note
where extnote_note = ''
  or extnote_note is null;

delete from external_note
where extnote_data_zdb_id in ('ZDB-GENO-031202-1','ZDB-GENO-050511-1');

delete from external_note
where extnote_data_zdb_id = 'ZDB-GENO-030501-1'
  and extnote_note not like 'The current%';

update external_note
  set extnote_note = scrub_char(extnote_note)
  where length(extnote_note) < 2048;

set constraints all deferred ;

set triggers for marker_relationship disabled ;
set triggers for marker_relationship_type disabled ;

update marker_relationship
  set mrel_type = 'contains other feature'
where mrel_type = 'contains special feature' ;

update marker_relationship_type
set mreltype_name = 'contains other feature'
where mreltype_name = 'contains special feature';

set triggers for marker_relationship enabled ;
set triggers for marker_relationship_type enabled ;

set constraints all immediate ;

insert into feature_assay (featassay_feature_zdb_id,  
    featassay_mutagen,  
    featassay_mutagee)
  select feature_zdb_id,
        "not specified",
        "not specified"
    from feature
    where not exists (select 'x'
                        from feature_assay
                        where featassay_feature_zdb_id =
                                feature_zdb_id);

set triggers for zdb_object_type disabled ;

set constraints all deferred ;

update zdb_object_type
  set zobjtype_home_table = 'feature'
  where zobjtype_home_table = 'alteration' ;

update zdb_object_type
  set zobjtype_home_zdb_id_column = 'feature_zdb_id'
  where zobjtype_home_table = 'feature' ;


update zdb_object_type
  set zobjtype_home_table = 'image'
  where zobjtype_home_table = 'fish_image' ;


update zdb_object_type
  set zobjtype_home_zdb_id_column = 'img_zdb_id'
  where zobjtype_home_table = 'image' ;


set triggers for zdb_object_type enabled ;


insert into zdb_active_data
  select genofeat_zdb_id
	from genotype_feature
	where not exists (Select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = genofeat_zdb_id);

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id,
		recattrib_source_type)
  select distinct genofeat_zdb_id, b.recattrib_source_zdb_id, 'standard'
   from genotype_feature, record_attribution b
   where genofeat_geno_zdb_id = b.recattrib_data_zdb_id
	and not exists (select 'x'
			 from record_Attribution a
			where a.recattrib_data_zdb_id = genofeat_zdb_id
			and a.recattrib_source_zdb_id = 
				b.recattrib_source_zdb_id
			and a.recattrib_source_type = b.recattrib_source_type);

insert into genotype_background (genoback_geno_zdb_id, 
					genoback_background_zdb_id)
values ('ZDB-GENO-060811-6','ZDB-GENO-990623-2');

--case 1446

create temp table tmp_hopkins (gzdb_id varchar(50))
with no log;

load from hopkins_deletion.txt
  insert into tmp_hopkins ;

create unique index hopind 
  on tmp_hopkins (gzdb_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_hopkins;
update statistics high for table genotype_feature;

update feature
  set feature_comments = null
  where exists (Select 'x'
		from genotype_Feature, tmp_hopkins
		where genofeat_geno_zdb_id =gzdb_id
		and feature_zdb_id = genofeat_feature_zdb_id);

--case 1363

create temp table tmp_lethals (gzdb_id varchar(50))
with no log;

load from lethal_fish.txt
  insert into tmp_lethals ;


update genotype_Feature
  set (genofeat_dad_zygocity, genofeat_mom_zygocity, genofeat_zygocity) = 
	(
		(select zyg_zdb_id
				from zygocity 
				where zyg_name = 'heterozygous'),
		(select zyg_zdb_id
				from zygocity 
				where zyg_name = 'heterozygous'),
		 (select zyg_zdb_id
				from zygocity 
				where zyg_name = 'homozygous')
	)
  where exists (select 'x'
		from tmp_lethals
		where gzdb_id = genofeat_geno_zdb_id) ;

create temp table tmp_lethalpato (apato_zdb_id varchar(50),
				apato_genox_zdb_id varchar(50),
				apato_start_stg_zdb_id varchar(50),
				apato_end_stg_zdb_id varchar(50),
				apato_pub_zdb_id varchar(50),
				apato_entity_a_zdb_id varchar(50),
				apato_quality_zdb_id varchar(50),
				apato_tag varchar(30))
with no log ;

insert into tmp_lethalpato (apato_zdb_id,
				apato_genox_zdb_id,
				apato_start_stg_zdb_id,
				apato_end_stg_zdb_id,
				apato_pub_zdb_id,
				apato_entity_a_zdb_id,
				apato_quality_zdb_id,
				apato_tag)
select get_id('APATO'),
	(Select genox_zdb_id
		from genotype_experiment
		where genox_geno_zdb_id = gzdb_id
		and genox_exp_zdb_id in (Select exp_zdb_id
					from experiment
					where exp_name = '_Standard')),
	(select stg_zdb_id
		from stage
		where stg_name = 'Unknown'),
	(select stg_zdb_id
		from stage
		where stg_name = 'Unknown'),
	'ZDB-PUB-060503-2',
	'ZDB-ANAT-050228-1',
	(select term_zdb_id
		from term
		where term_ont_id = 'PATO:0000718'
		and term_is_obsolete != 't'),
	'abnormal'
   from tmp_lethals;

insert into atomic_phenotype (apato_zdb_id,
				apato_genox_zdb_id,
				apato_start_stg_zdb_id,
				apato_end_stg_zdb_id,
				apato_pub_zdb_id,
				apato_entity_a_zdb_id,
				apato_quality_zdb_id,
				apato_tag)
  select apato_zdb_id,
				apato_genox_zdb_id,
				apato_start_stg_zdb_id,
				apato_end_stg_zdb_id,
				apato_pub_zdb_id,
				apato_entity_a_zdb_id,
				apato_quality_zdb_id,
				apato_tag 
  from tmp_lethalpato ;	 


insert into zdb_active_data
  select apato_zdb_id
	from atomic_phenotype
	where not exists (Select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = apato_zdb_id);

create temp table tmp_fig (fig_id varchar(50),
				fig_fish_id varchar(50),
				fig_source varchar(50))
 with no log;

insert into tmp_fig 
  select fig_zdb_id, fig_comments, fig_source_zdb_id
    from figure
    where fig_comments like 'ZDB-%';

create unique index fig_index on tmp_Fig (fig_id)
  using btree in idxdbs3 ;

create index fig_fish_index on tmp_fig (fig_fish_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_fig ;

delete from tmp_fig
  where not exists (Select 'x'
			from tmp_lethals
			where gzdb_id = fig_fish_id);

insert into apato_figure (apatofig_apato_zdb_id,
			apatofig_fig_zdb_id)
  select distinct apato_zdb_id, fig_id
    from atomic_phenotype, genotype_experiment, tmp_fig
    where apato_genox_zdb_id = genox_zdb_id
    and genox_geno_zdb_id = fig_fish_id 
    and not exists (Select 'x'
			from apato_figure
			where apatofig_fig_zdb_id = fig_id
			and apatofig_apato_zdb_id = apato_zdb_id);

select count(*), apatofig_fig_zdb_id, apatofig_apato_zdb_id
  from apato_Figure
  group by  apatofig_fig_zdb_id, apatofig_apato_zdb_id
  having count(*) > 1;

!echo "NEED placholder FIGURES" ;

select first 10 apato_genox_zdb_id, geno_zdb_id, geno_handle
  from atomic_phenotype, genotype_experiment, genotype
  where not exists (Select 'x'
			from apato_figure
			where apatofig_apato_zdb_id =
			     apato_zdb_id)
  and apato_genox_zdb_id = genox_zdb_id
  and genox_geno_zdb_id = geno_zdb_id;

--Tg nomenclature change

--update feature
--  set feature_name = feature_abbrev
--  where feature_name like 'Tg(%' ;

select count(*), geno_handle
  from genotype
  group by geno_handle
  having count(*) > 1;

set constraints all immediate; 

update feature
  set feature_type = "TRANSGENIC_INSERTION"
  where feature_name like 'Tg%' 
  and feature_type != "TRANSGENIC_INSERTION";

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select geno_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from genotype
   where not exists (select 'x'
			from record_Attribution b
			where b.recattrib_data_zdb_id = geno_Zdb_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-030129-1'
			and b.recattrib_source_type = 'standard');

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select feature_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from feature
   where not exists (select 'x'
			from record_Attribution b
			where b.recattrib_data_zdb_id = feature_Zdb_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-030129-1'
			and b.recattrib_source_type = 'standard');

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select mrkr_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from marker
   where not exists (select 'x'
			from record_Attribution b
			where b.recattrib_data_zdb_id = mrkr_Zdb_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-030129-1'
			and b.recattrib_source_type = 'standard');


alter table genotype_feature
  drop genofeat_chromosome; 

drop trigger feature_marker_relationship_update_Trigger;

alter table genotype
  add (geno_nickname varchar(255));

update genotype
  set geno_nickname = geno_handle ;

create temp table tmp_nomen (mhist_zdb_id varchar(50))
	with no log ;

insert into tmp_nomen
  select mhist_zdb_id from marker_history ;

create unique index nomen_i on tmp_nomen (mhist_zdb_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_nomen ;

delete from zdb_active_Data
  where zactvd_zdb_id like 'ZDB-NOMEN-%'
and not exists (select 'x'
		from tmp_nomen
		where zactvd_zdb_id = mhist_zdb_id); 

set constraints all immediate ;


commit work ;
--rollback work ;