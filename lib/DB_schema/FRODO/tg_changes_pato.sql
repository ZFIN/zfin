begin work ;

set constraints all deferred ;

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

set constraints all immediate ;
commit work;

--rollback work ;
