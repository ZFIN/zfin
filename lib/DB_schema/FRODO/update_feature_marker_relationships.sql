begin work ;

delete from genotype_feature
where genofeat_geno_zdb_id = 'ZDB-GENO-060811-13' 
and genofeat_feature_zdb_id = 'ZDB-ALT-980203-1235'
and genofeat_zdb_id not like 'ZDB-GENOFEAT-060811-%';

set constraints all deferred ;

--update feature
--  set feature_type = 'TRANSVERSION'
--  and feature_name = 'tr217';

--update feature
--  set feature_type = 'TRANSVERSION'
--  where feature_name = 'tr217'; 

delete from feature_marker_relationship_type
where fmreltype_name = 'contains sequence feature'
		and fmreltype_ftr_type_group = 'MUTANT'
		and fmreltype_mrkr_type_group = 'CONSTRUCT';

insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('contains sequence feature', 'TG_INSERTION','CONSTRUCT','Contains','Contained in');



set constraints all immediate ;

delete from genotype_Feature
  where genofeat_geno_zdb_id = 'ZDB-GENO-060811-13'
  and genofeat_feature_zdb_id = 'ZDB-ALT-980203-1235'
  and genofeat_zygocity = 'ZDB-ZYG-061214-1'
  and genofeat_mom_zygocity = 'ZDB-ZYG-061214-7'
  and genofeat_dad_zygocity = 'ZDB-ZYG-061214-7' ;


update statistics for procedure ;

set constraints all deferred ;

insert into zdb_active_data
  select genofeat_zdb_id
	from genotype_feature
	where not exists (Select 'x'
			    from zdb_active_data
				where zactvd_zdb_id = genofeat_zdb_id);
set constraints all immediate ;

select count(*),genofeat_geno_zdb_id, genofeat_feature_zdb_id,
			genofeat_zygocity, genofeat_mom_zygocity,
			genofeat_dad_zygocity
  from genotype_feature
group by genofeat_geno_zdb_id, genofeat_feature_zdb_id,
			genofeat_zygocity, genofeat_mom_zygocity,
			genofeat_dad_zygocity
  having count(*) > 1; 

select * from genotype_feature
 where genofeat_geno_zdb_id = 'ZDB-GENO-060811-13' ;

create unique index genotype_Feature_alternate_key_index
 on genotype_Feature (genofeat_geno_zdb_id, genofeat_feature_zdb_id,
			genofeat_zygocity, genofeat_mom_zygocity,
			genofeat_dad_zygocity)
 using btree in idxdbs3 ;

alter table genotype_feature
  add constraint unique (genofeat_geno_zdb_id, genofeat_feature_zdb_id,
			genofeat_zygocity, genofeat_mom_zygocity,
			genofeat_dad_zygocity)
  constraint genofeat_alternate_key ;

-- replace larva with whole organism and change stage to larva stages
update atomic_phenotype
  set apato_entity_a_zdb_id = "ZDB-ANAT-050228-1",
      apato_start_stg_zdb_id = "ZDB-STAGE-010723-8",
      apato_end_stg_zdb_id = "ZDB-STAGE-010723-49"
where apato_zdb_id = "ZDB-APATO-061122-11453";


-- replace cranial motoneurons to primary motoneurons

update atomic_phenotype
  set apato_entity_a_zdb_id = "ZDB-ANAT-011113-498"
where apato_zdb_id = "ZDB-APATO-061122-2886";

--case 1463
--there are no orphans!?

delete from zdb_active_data
  where zactvd_zdb_id like 'ZDB-LOCUS-%' ;

--alter table atomic_phenotype
--  drop constraint apato_zdb_active_data_foreign_key_odc ;

--alter table apato_figure
--  drop constraint apatofig_pato_foreign_key_odc ;


--!echo "COUNTS From APATO_FIGURE" ;

--select count(*) from apato_Figure ;

--select count(*) from zdb_active_data
--  where zactvdb_zdb_id like 'ZDB-APATO-%'
-- and not exists (select 'x'
--		  from atomic_phenotype
--			where apato_zdb_id = zactvd_zdb_id);

--delete from zdb_active_data
--  where not exists (select 'x'
--			from atomic_phenotype
--			where apato_zdb_id = zactvd_zdb_id)
--  and zactvd_zdb_id like 'ZDB-APATO-%' ;

--select count(*) from apato_Figure ;
--
--alter table atomic_phenotype 
--  add constraint (foreign key (apato_zdb_id)
--  references zdb_active_data on delete cascade constraint 
--	apato_zdb_id_foreign_key_odc) ;

--alter table apato_figure
--  add constraint (foreign key (apatofig_apato_zdb_id)
--  references atomic_phenotype on delete cascade constraint 
--	apatofig_apato_zdb_id_foreign_key_odc) ;


--case 1489

set constraints all deferred ;

delete from feature_marker_relationship_type
  where fmreltype_name = 'contains sequence feature' ;

insert into feature_marker_relationship_type (fmreltype_name,
	fmreltype_ftr_type_group,
	fmreltype_mrkr_type_group,
	fmreltype_1_to_2_comments,
	fmreltype_2_to_1_comments)
values ('contains sequence feature', 'TG_INSERTION','CONSTRUCT','Contains','Contained in');

select * from feature_marker_relationship_type
  where not exists (Select 'x'
			from feature_type_group
			where fmreltype_ftr_type_group = ftrgrp_name);

set constraints all immediate ;

--case 1495

update genotype_feature
  set genofeat_zygocity = (select zyg_zdb_id 
				from zygocity
				where zyg_name = 'homozygous')
  where genofeat_zygocity = (select zyg_zdb_id
				from zygocity 
				where zyg_name = 'maternal zygotic');

update genotype_feature
  set genofeat_zygocity = (select zyg_zdb_id 
				from zygocity
				where zyg_name = 'homozygous')
  where genofeat_zygocity = (select zyg_zdb_id
				from zygocity 
				where zyg_name = 'paternal zygotic');


update genotype_feature
  set genofeat_zygocity = (select zyg_zdb_id 
				from zygocity
				where zyg_name = 'homozygous')
  where genofeat_zygocity = (select zyg_zdb_id
				from zygocity 
				where zyg_name = 'hemizygous');
delete from zygocity
  where zyg_name = 'maternal zygotic' ;

delete from zygocity
  where zyg_name = 'paternal zygotic' ;

delete from zygocity
  where zyg_name = 'hemizygous' ;

insert into zdb_active_data
  select zyg_zdb_id from zygocity 
    where not exists (Select 'x'
			from zdb_active_data
			where zactvd_zdb_id = zyg_zdb_id);

update statistics for procedure ;

alter table zygocity
  add constraint (foreign key (zyg_zdb_id)
  references zdb_active_data constraint
  zygocity_zdb_active_data_foreign_key) ;

create temp table tmp_obj (number int)
with no log ;

insert into tmp_obj (number)
 select count(*)
   from genotype
   where geno_zdb_id like 'ZDB-GENO-070105-%' ;

update tmp_obj
  set number = number+1;

update zdb_object_type
  set zobjtype_seq = (Select number from tmp_obj)
  where zobjtype_name = 'GENO'
  and zobjtype_seq = '1' ;

update zdb_object_type
  set zobjtype_day = CURRENT
  where zobjtype_name = 'GENO';

--rollback work ;

commit work ;