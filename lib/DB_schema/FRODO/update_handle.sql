begin work ;

--update feature
-- set feature_abbrev = feature_name
--  where feature_abbrev != feature_name 
-- and feature_zdb_id != 'ZDB-ALT-041105-2';

--update feature
--  set feature_abbrev = feature_name
--  where feature_abbrev !=feature_name ;

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

select get_genotype_display(genotype_id)
  from tmp_genotype ;

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

set constraints all deferred ;

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

set constraints all immediate ;

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
set constraints all immediate; 

update feature
  set feature_type = "INSERTION"
  where feature_name like 'Tg%' 
  and feature_type != "INSERTION";

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select geno_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from genotype
   where not exists (select 'x'
			from record_Attribution
			where recattrib_data_zdb_id = geno_Zdb_id);

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select feature_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from feature
   where not exists (select 'x'
			from record_Attribution
			where recattrib_data_zdb_id = feature_Zdb_id);

insert into record_Attribution (recattrib_data_zdb_id, 
				recattrib_source_zdb_id,
				recattrib_source_type)
  select mrkr_zdb_id, 'ZDB-PUB-030129-1', 'standard'
   from marker
   where not exists (select 'x'
			from record_Attribution
			where recattrib_data_zdb_id = mrkr_Zdb_id);


alter table genotype_feature
  drop genofeat_chromosome; 

drop trigger feature_marker_relationship_update_Trigger;

alter table genotype
  add (geno_nickname varchar(255));

update genotype
  set geno_nickname = geno_handle ;



--commit work ;

rollback work ;
