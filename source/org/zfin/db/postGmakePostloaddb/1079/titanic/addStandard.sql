--liquibase formatted sql
--changeset sierra:addStandard

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
 select id, expid, "ZECO:0000103"  
    from tmp_expcond
 where not exists (SElect 'x' from experiment
       	   	  	  where exp_zdb_id = expid);


