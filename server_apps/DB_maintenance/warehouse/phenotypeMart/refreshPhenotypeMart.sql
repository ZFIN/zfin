--drop FKs.


truncate phenotype_source_generated_bkup;
truncate phenotype_observation_generated_bkup;

insert into phenotype_source_generated_bkup (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_genearted;

insert into phenotype_observation_generted_bkup (psg_id,
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

truncate phenotype_source_generated;
truncate phenotype_observation_generated;

insert into phenotype_source_generated (pg_id,
       	     			 pg_genox_zdb_id,
				 pg_fig_zdb_id,
				 pg_start_stg_zdb_id,
				 pg_end_stg_zdb_id)
select pg_id, pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id
  from phenotype_source_genearted_temp;

insert into phenotype_observation_generted (psg_id,
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

create unique index phenotype_source_generated_pk_index (pg_id)
 using btree in idxdbs2;

create index phenotype_source_generated_fig_index (pg_fig_zdb_id)
 using btree in idxdbs2;

create index phenotype_source_generated_genox_index (pg_genox_zdb_id)
 using btree in idxdbs3;

create unique index phenotype_observation_generated_pk_id_index (psg_id)
using btree in idxdbs1;

create index phenotype_observation_generated_psg_pg_id_index (psg_pg_id)
using btree in idxdbs3;

create index phenotype_observation_generated_mrkr_zdb_index (psg_mrkr_zdb_id)
using btree in idxdbs3;

create index phenotype_observation_generated_e1a_zdb_index (psg_e1a_zdb_id)
using btree in idxdbs2;

create index phenotype_observation_generated_e1b_zdb_index (psg_e1b_zdb_id)
using btree in idxdbs2;

create index phenotype_observation_generated_e1b_zdb_index (psg_e2a_zdb_id)
using btree in idxdbs1;

create index phenotype_observation_generated_e1b_zdb_index (psg_e2b_zdb_id)
using btree in idxdbs1;

create index phenotype_observation_generated_quality_zdb_index (psg_quality_zdb_id)
using btree in idxdbs3;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_phenotypemart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "phenotype mart";
