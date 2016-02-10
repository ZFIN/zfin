--drop FKs.


truncate phenotype_source_generated_bkup;
commit work;
begin work;
truncate phenotype_observation_generated_bkup;
commit work;
begin work;
insert into phenotype_source_generated_bkup (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_generated;

insert into phenotype_observation_generated_bkup (psg_id,
       	     				   psg_pg_id,
       	     				   psg_mrkr_zdb_id,	
					   psg_mrkr_abbrev,
					   psg_mrkr_relation,
					   psg_e1a_zdb_id,
					   psg_e1a_name,
					   psg_e1_relation_name,	
					   psg_e1b_zdb_id,
					   psg_e1b_name,
					   psg_e2a_zdb_id,
					   psg_e2a_name,	
					   psg_e2_relation_name,
					   psg_e2b_zdb_id,
					   psg_e2b_name,
					   psg_tag,
					   psg_quality_zdb_id,
					   psg_quality_name,
					   psg_short_name)
select psg_id, psg_pg_id,psg_mrkr_zdb_id, psg_mrkr_abbrev,psg_mrkr_relation,psg_e1a_zdb_id,
	psg_e1a_name,psg_e1_relation_name, psg_e1b_zdb_id, psg_e1b_name,
	psg_e2a_zdb_id,psg_e2a_name, psg_e2_relation_name, psg_e2b_zdb_id,
	psg_e2b_name,psg_tag,psg_quality_zdb_id, psg_quality_name, psg_short_name
  from phenotype_observation_generated;


update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_phenotypemart" ;

commit work;
begin work;
truncate phenotype_source_generated;
commit work;
begin work;
truncate phenotype_observation_generated;
commit work;
begin work;

insert into phenotype_source_generated (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_generated_temp;

insert into phenotype_observation_generated (psg_id,
       	     				   psg_pg_id,
       	     				   psg_mrkr_zdb_id,	
					   psg_mrkr_abbrev,
					   psg_mrkr_relation,
					   psg_e1a_zdb_id,
					   psg_e1a_name,
					   psg_e1_relation_name,	
					   psg_e1b_zdb_id,
					   psg_e1b_name,
					   psg_e2a_zdb_id,
					   psg_e2a_name,	
					   psg_e2_relation_name,
					   psg_e2b_zdb_id,
					   psg_e2b_name,
					   psg_tag,
					   psg_quality_zdb_id,
					   psg_quality_name,
					   psg_short_name)
select psg_id, psg_pg_id,psg_mrkr_zdb_id, psg_mrkr_abbrev,psg_mrkr_relation,psg_e1a_zdb_id,
	psg_e1a_name,psg_e1_relation_name, psg_e1b_zdb_id, psg_e1b_name,
	psg_e2a_zdb_id,psg_e2a_name, psg_e2_relation_name, psg_e2b_zdb_id,
	psg_e2b_name,psg_tag,psg_quality_zdb_id, psg_quality_name, psg_short_name
  from phenotype_observation_generated_temp;



update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_phenotypemart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "phenotype mart";
