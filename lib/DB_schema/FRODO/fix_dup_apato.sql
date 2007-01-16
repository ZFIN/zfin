begin work ;

create table tmp_apato_full_no_dups (
				apatod_genox_zdb_id varchar(50),
				apatod_start_stage_zdb_id varchar(50),
				apatod_end_stage_zdb_id varchar(50),
				apatod_entity_1_zdb_id varchar(50),
				apatod_quality varchar(50),
				apatod_tag varchar(25),
				counter int)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
extent size 2048 
next size 2048 lock mode page;

insert into tmp_apato_full_no_dups (
				apatod_genox_zdb_id,
				apatod_start_stage_zdb_id,
				apatod_end_stage_zdb_id,
				apatod_entity_1_zdb_id,
				apatod_tag, 
				counter )

select apato_genox_zdb_id, 
	apato_start_stg_zdb_id,
	apato_end_stg_zdb_id, 
	apato_entity_a_zdb_id,
	apato_tag, 
	count(*)
 from atomic_phenotype
  group by apato_genox_zdb_id, 
	apato_start_stg_zdb_id,
	apato_end_stg_zdb_id, 
	apato_entity_a_zdb_id,
        apato_tag
  having count(*) > 1 ;

--select first 10 * from tmp_apato_full_no_dups ;
		
delete from zdb_active_data
  where exists (select 'x'
  		  from tmp_apato_full_no_dups, atomic_phenotype
		  where apato_genox_zdb_id = apatod_genox_zdb_id
  and	apato_start_stg_zdb_id = apatod_start_stage_zdb_id
  and	apato_end_stg_zdb_id = apatod_end_stage_zdb_id 
  and	apato_entity_a_zdb_id = apatod_entity_1_zdb_id
  and	apato_tag = apatod_tag 
  and apato_quality_zdb_id = (select term_zdb_id
				  from term
				  where term_name = 'quality'
				  and term_is_obsolete = 'f')
  and apato_zdb_id = zactvd_zdb_id)
  and zactvd_zdb_id like 'ZDB-APATO-%';

drop table tmp_apato_full_no_dups ;
commit work ;

--rollback work ;