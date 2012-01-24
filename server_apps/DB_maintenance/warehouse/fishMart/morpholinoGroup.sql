begin work ;

!echo "begin morpholino group";

insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id);


insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and not exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

insert into morpholino_group (morphg_genox_Zdb_id)
  select distinct genox_zdb_id from genotype_experiment
   where exists (select 'x' from experiment_condition
   	 		where expcond_exp_zdb_id = genox_exp_zdb_id
   	 		and expcond_mrkr_zdb_id is not null)
   and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
   and exists (Select 'x' from expression_Experiment where xpatex_genox_zdb_id = genox_Zdb_id);

update morpholino_group (morphg_group_name)
  set morphg_group_name = replace(replace(replace(substr(multiset (select distinct 
						  	  item marker.mrkr_abbrev from marker, genotype_experiment,
							  experiment_Condition, genotype
  	 	   	   				  where expcond_exp_zdb_id = genox_exp_zdb_id
							  and geno_zdb_id = genox_geno_zdb_id
							   and expcond_mrkr_Zdb_id = mrkr_zdb_id
							  and genox_zdb_id = morphg_genox_zdb_id
							  order by marker.mrkr_abbrev)::lvarchar(380),11),""),"'}",""),"'","");



update morpholino_group
  set morphg_group_name = replace(morphg_group_name,",M","+M")
  where morphg_Group_name like '%,%';

insert into morpholino_group_member(morphgm_group_id, morphgm_member_name, morphgm_member_id)
  select morphg_group_pk_id, mrkr_abbrev, mrkr_Zdb_id
    from marker, genotype_experiment, experiment_Condition, morpholino_group
    where expcond_exp_zdb_id = genox_exp_zdb_id
   and expcond_mrkr_Zdb_id = mrkr_zdb_id
   and morphg_genox_zdb_id = genox_zdb_id;


!echo "lvarchar length for morpholino group";

select max(octet_length(morphg_group_name))
 from morpholino_group;




--rollback work ;

commit work ;