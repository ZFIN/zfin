begin work ;

set constraints all deferred ;


create temp table tmp_mis_fig (fig_id varchar(50),
				geno_id varchar(50),
				pub_id varchar(50))
  with no log ;

insert into tmp_mis_fig (geno_id, fig_id, pub_id)
 select distinct geno_Zdb_id, fig_Zdb_id, fig_source_zdb_id
   from figure, genotype
  where fig_comments like 'ZDB-GENO-%'  
  and fig_comments = geno_zdb_id
  and exists (Select 'x'
                from image
                where fig_zdb_id = img_fig_zdb_id)
  and not exists (select 'x'
                        from apato_figure
                        where apatofig_fig_zdb_id = fig_zdb_id) ;

update tmp_mis_fig
  set geno_id = (select genox_zdb_id
			from genotype_experiment
			where genox_geno_Zdb_id = geno_id
			and exists (select 'x'
					from experiment
					where exp_zdb_id = genox_exp_zdb_id
					and exp_name = '_Standard'));

insert into atomic_phenotype (apato_zdb_id,
				apato_genox_zdb_id,
				apato_entity_a_zdb_id,
				apato_quality_zdb_id,
				apato_start_Stg_zdb_id,
				apato_end_stg_zdb_id,
				apato_tag,
				apato_pub_zdb_id)
select get_id('APATO'),
         geno_id,
	 (select anatitem_zdb_id
		from anatomy_item
		where anatitem_name = 'whole organism'),
	 (select term_zdb_id
		from term
		where term_name = 'quality'
		and term_is_obsolete = 'f'),
	 (select stg_zdb_id 
		from stage
		where stg_name = 'Unknown'),
	 (select stg_zdb_id 
		from stage
		where stg_name = 'Unknown'),
	 'abnormal',
	pub_id
  from tmp_mis_fig ;

update feature
  set feature_type = 'TRANSGENIC_INSERTION'
  where feature_type = 'INSERTION'
  and feature_name not in ('tc317','tc240','m209','z12','c99','cz35', 't3','tc317e','b160','b195','tx201','te275','te370e');

update atomic_phenotype
  set (apato_quality_zdb_id, apato_tag) = ((Select term_zdb_id
			from term
			where term_name = 'pattern'),'present')
  where exists (Select 'x'
		  from genotype_experiment, genotype_feature,
			feature
		  where genox_geno_zdb_id = genofeat_geno_zdb_id
		  and genofeat_feature_zdb_id = feature_zdb_id
		  and feature_type = 'TRANSGENIC_INSERTION'
		  and feature_name like 'Tg(%'
		  and genox_zdb_id = apato_genox_zdb_id)
  and apato_quality_zdb_id = (select term_zdb_id
				from term
				where term_name = 'quality'
				and term_is_obsolete = 'f');

insert into zdb_active_data
  select apato_zdb_id
	from atomic_phenotype
	where not exists (select 'x'
				from zdb_active_data
				where zactvd_zdb_id = apato_zdb_id);

insert into record_attribution (recattrib_data_zdb_id,
				recattrib_source_zdb_id,
				recattrib_source_type)
  select distinct apato_zdb_id,
	'ZDB-PUB-060503-2',
	'standard'
    from atomic_phenotype
    where not exists (Select 'x'
			from record_Attribution b
			where b.recattrib_data_zdb_id = apato_zdb_id
			and b.recattrib_source_zdb_id = 'ZDB-PUB-060503-2'
			and b.recattrib_source_type = 'standard'
			);

insert into apato_figure (apatofig_apato_zdb_id,
			apatofig_fig_zdb_id)
  select apato_zdb_id,
	fig_id
    from tmp_mis_fig, atomic_phenotype
    where apato_genox_zdb_id = geno_id;


set constraints all immediate ;
commit work ;

--rollback work ;