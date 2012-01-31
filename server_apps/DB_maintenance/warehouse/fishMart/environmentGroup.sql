begin work ;

!echo "begin environment group";

insert into environment_group (eg_genox_zdb_id)
  select distinct genox_zdb_id from genotype_experiment
    where exists (select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
    or exists (select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id)
    order by genox_zdb_id;

insert into environment_group (eg_genox_zdb_id)
  select distinct genox_zdb_id from genotype_experiment
    where not exists (select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and not exists (select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id)
   order by genox_Zdb_id;


update environment_group (eg_group_name)
  set eg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item cdt_name||" "||cdt_group from genotype_experiment,
							  experiment_Condition, condition_Data_type
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and cdt_zdb_id = expcond_cdt_zdb_id
							  and genox_zdb_id = eg_genox_zdb_id
							  )::lvarchar(380),11),""),"'}",""),"'","");

--select first 5 * from environment_group;

insert into environment_group_member(egm_group_id, egm_member_name, egm_member_id)
  select eg_group_pk_id, cdt_name||" "||cdt_group, genox_zdb_id
    from condition_Data_type, genotype_experiment, experiment_Condition, environment_group
    where expcond_exp_zdb_id = genox_exp_zdb_id
   and expcond_cdt_Zdb_id = cdt_zdb_id
   and eg_genox_zdb_id = genox_zdb_id;

!echo "lvarchar length for environment group";

select max(octet_length(eg_group_name))
 from environment_group;

commit work;
--rollback work ;