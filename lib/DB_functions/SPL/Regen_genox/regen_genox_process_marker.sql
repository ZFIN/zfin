create procedure regen_genox_process_marker()

  -- --------------------------------------------------------------------------------------------
  -- Generates marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table 
  --
  -- PRECONDITONS:
  --   regen_genox_input_zdb_id_temp table exists and contains a list of gene and/or MO ZDB IDs
  --     to generate marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table
  --   regen_genox_temp table exists
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  -- --------------------------------------------------------------------------------------------

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct fmrel_mrkr_zdb_id , genox_zdb_id
    from fish, fish_experiment, feature_marker_relationship, genotype_Feature, regen_genox_input_zdb_id_temp,feature
    where fish_zdb_id = genox_fish_Zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and fmrel_ftr_zdb_id=feature_zdb_id
    and feature_type not in ('DEFICIENCY','TRANSLOC')
    and fish_functional_affected_gene_count = 1
    and get_obj_type(fmrel_mrkr_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG')
    and genox_is_std_or_generic_control = 't'
    and fmrel_mrkr_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fmrel_mrkr_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, genox_zdb_id
    from fish, fish_str a, fish_experiment, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
 and fish_functional_affected_gene_count = 1
    and genox_is_std_or_generic_control = 't'
    and fishstr_str_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where a.fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
  and not exists (Select 'x' from fish_str b
                            where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
                            and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id and b.fishstr_str_zdb_id not in (select mrel_mrkr_1_zdb_id from marker_relationship where mrel_mrkr_2_zdb_id='ZDB-GENE-990415-270' and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'));

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select mrel_mrkr_2_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment, marker_relationship, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count = 1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG')
 and genox_is_std_or_generic_control = 't' 
   and mrel_mrkr_2_zdb_id = rggz_zdb_id and mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
 and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
			 select mrel_mrkr_2_zdb_id, genox_zdb_id
    from marker_relationship, fish_str a,fish,genotype,fish_experiment, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count = 1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG')
 and genox_is_std_or_generic_control = 't'
 and fish_genotype_zdb_id=geno_zdb_id
                     and geno_is_wildtype='t'
   and mrel_mrkr_2_zdb_id = rggz_zdb_id  and mrel_mrkr_2_zdb_id ='ZDB-GENE-990415-270' and  not exists (Select 'x' from fish_str b
                            where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
                            and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id)
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);


insert into regen_genox_construct_temp (rgct_construct_zdb_id, rgct_genox_zdb_id)
select   b.fmrel_mrkr_zdb_id,genox_zdb_id
from fish_experiment, fish,genotype_feature, feature_marker_relationship b,experiment_condition,all_term_contains,marker
where genox_fish_zdb_id = fish_Zdb_id
and fish_genotype_zdb_id = genofeat_geno_zdb_id
and genox_exp_zdb_id = expcond_exp_zdb_id
and expcond_zeco_term_Zdb_id = alltermcon_contained_zdb_id
and alltermcon_container_zdb_id ='ZDB-TERM-160831-68'
and b.fmrel_ftr_zdb_id = genofeat_feature_zdb_id
and fmrel_mrkr_zdb_id = mrkr_zdb_id
and  mrkr_abbrev like 'Tg%(%hsp70%';

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select   b.fmrel_mrkr_zdb_id,rgct_genox_zdb_id
from fish_experiment, fish,genotype_feature, feature_marker_relationship b,regen_genox_input_zdb_id_temp,regen_genox_construct_temp
where genox_fish_zdb_id = fish_Zdb_id
and fish_genotype_zdb_id = genofeat_geno_zdb_id
and b.fmrel_ftr_zdb_id = genofeat_feature_zdb_id  and  fmrel_mrkr_zdb_id = rggz_zdb_id
and  genox_zdb_id=rgct_genox_zdb_id;

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, rgct_genox_zdb_id
    from fish, fish_str a, fish_experiment, regen_genox_input_zdb_id_temp,regen_genox_construct_temp
        where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
 and genox_zdb_id=rgct_genox_zdb_id
    and fishstr_str_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where a.fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
 and not exists (Select 'x' from fish_str b
     	 		    where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
			    and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id);


	insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, rgct_genox_zdb_id
    from fish, fish_str a, fish_experiment, regen_genox_input_zdb_id_temp,regen_genox_construct_temp
        where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
 and genox_zdb_id=rgct_genox_zdb_id
    and fishstr_str_zdb_id = rggz_zdb_id
and fishstr_fish_zdb_id = fish_zdb_id and a.fishstr_str_zdb_id not in (select mrel_mrkr_1_zdb_id from marker_relationship where mrel_mrkr_2_zdb_id='ZDB-GENE-990415-270' and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%');

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select mrel_mrkr_2_zdb_id, rgct_genox_zdb_id
    from fish, fish_str, fish_experiment, marker_relationship, regen_genox_input_zdb_id_temp,regen_genox_construct_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
     and genox_zdb_id=rgct_genox_zdb_id
     and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG')
    and mrel_mrkr_2_zdb_id = rggz_zdb_id
    and mrel_mrkr_2_zdb_id = rggz_zdb_id and mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
 and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

delete from regen_genox_temp
  where exists (Select 'x' from fish_experiment, fish,genotype_feature, feature_marker_relationship
  	       	       where rggt_genox_zdb_id = genox_zdb_id
		       and genox_fish_zdb_id = fish_Zdb_id
		       and fish_genotype_zdb_id = genofeat_geno_zdb_id
		       and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
		       and fmrel_type = 'contains phenotypic sequence feature');


	delete from regen_genox_temp
	  where exists (Select 'x' from fish_experiment, fish,genotype_feature, feature_marker_relationship,feature
  	       	       where rggt_genox_zdb_id = genox_zdb_id
		       and genox_fish_zdb_id = fish_Zdb_id
		       and fish_genotype_zdb_id = genofeat_geno_zdb_id
		       and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
		       and fmrel_ftr_zdb_id = feature_zdb_id and feature_type in ('DEFICIENCY','TRANSLOC'));

--    end if

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
