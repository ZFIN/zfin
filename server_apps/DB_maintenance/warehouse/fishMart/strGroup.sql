
!echo "begin str group";

insert into str_group (strg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id);


insert into str_group (strg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and not exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

insert into str_group (strg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

update str_group (strg_group_name)
  set strg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item marker.mrkr_abbrev from marker, genotype_experiment,
							  experiment_Condition, genotype
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							   and expcond_mrkr_Zdb_id = mrkr_zdb_id
							  and genox_zdb_id = strg_genox_zdb_id
							  order by marker.mrkr_abbrev)::lvarchar(380),11),""),"'}",""),"'","");



update str_group
  set strg_group_name = replace(strg_group_name,",M","+M")
  where strg_Group_name like '%,%';

update str_group
  set strg_group_name = replace(strg_group_name,",T","+T")
  where strg_Group_name like '%,%';

update str_group
  set strg_group_name = replace(strg_group_name,",C","+C")
  where strg_Group_name like '%,%';

insert into str_group_member(strgm_group_id, strgm_member_name, strgm_member_id)
  select strg_group_pk_id, mrkr_abbrev, mrkr_Zdb_id
    from marker, genotype_experiment, experiment_Condition, str_group
    where expcond_exp_zdb_id = genox_exp_zdb_id
   and expcond_mrkr_Zdb_id = mrkr_zdb_id
   and strg_genox_zdb_id = genox_zdb_id;


!echo "lvarchar length for str group";

select max(octet_length(strg_group_name))
 from str_group;


