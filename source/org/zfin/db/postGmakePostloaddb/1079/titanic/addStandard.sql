--liquibase formatted sql
--changeset sierra:addStandard

delete from experiment_condition
 where expcond_zdb_id = 'ZDB-EXPCOND-041102-2';

create temp table tmp_expcond (id varchar(50), expid varchar(50))
with no log;

insert into tmp_expcond (id, expid)
 select get_id('EXPCOND'), exp_zdb_id
   from experiment 
  where not exists (Select 'x' from experiment_condition
  	    	   	   where expcond_exp_Zdb_id = exp_zdb_id);

insert into zdb_active_data
  select id from tmp_expcond;

insert into experiment_condition (Expcond_zdb_id, expcond_exp_zdb_id,
       	    			 		  expcond_zeco_term_Zdb_id)
 select id, expid, (select term_zdb_id from term where term_ont_id ="ZECO:0000103")
    from tmp_expcond;


