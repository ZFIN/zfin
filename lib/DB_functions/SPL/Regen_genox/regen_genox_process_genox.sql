create procedure regen_genox_process_genox()

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


-- The genotypes include any of the WT lines, which (cannot be with 'Standard' or 'Generic Control' environments),
-- and has MO(s) which target ONLY this gene
--exclude features of type DEFICIENCY, TRANSLOCATION and INVERSION
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select distinct fmrel_mrkr_zdb_id , genox_zdb_id
    from fish, fish_experiment, feature_marker_relationship, genotype_Feature, feature,regen_genox_input_zdb_id_temp
    where fish_zdb_id = genox_fish_Zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and genofeat_feature_zdb_id=feature_zdb_id
    and feature_type not in ('DEFICIENCY','TRANSLOC')
    and fish_functional_affected_gene_count = 1
 and get_obj_type(fmrel_mrkr_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG')
    and genox_is_std_or_generic_control = 't'
    and genox_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fmrel_mrkr_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, genox_zdb_id
    from fish, fish_str a, fish_experiment,feature_marker_relationship, genotype_Feature, feature, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and genofeat_feature_zdb_id=feature_zdb_id
    and feature_type not in ('DEFICIENCY','TRANSLOC')
    and fish_functional_affected_gene_count =1
    and genox_is_std_or_generic_control = 't'
    and genox_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
 and not exists (Select 'x' from fish_str b 
     	 		    where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
			    and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
select mrel_mrkr_2_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment,feature_marker_relationship, genotype_Feature, feature, marker_relationship, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and genofeat_feature_zdb_id=feature_zdb_id
    and feature_type not in ('DEFICIENCY','TRANSLOC')
    and fish_functional_affected_gene_count =1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE')
 and genox_is_std_or_generic_control = 't' 
   and genox_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where mrel_mrkr_2_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id);

insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fishstr_str_zdb_id, genox_zdb_id
    from fish, fish_str a, fish_experiment,feature_marker_relationship, genotype_Feature, feature, experiment_condition,marker,all_term_contains, regen_genox_input_zdb_id_temp
    where fish_Zdb_id =a.fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and genofeat_feature_zdb_id=feature_zdb_id
    and feature_type not in ('DEFICIENCY','TRANSLOC')
    and fish_functional_affected_gene_count =1
    and genox_exp_zdb_id = expcond_exp_zdb_id
     and genox_exp_zdb_id = expcond_exp_zdb_id
     and expcond_zeco_term_Zdb_id = alltermcon_contained_zdb_id
        and alltermcon_container_zdb_id ='ZDB-TERM-160831-68'
    and fmrel_mrkr_zdb_id =mrkr_zdb_id
    and mrkr_abbrev like '%Tg(%hsp70%)'
    and genox_zdb_id = rggz_zdb_id
and not exists (Select 'x' from regen_genox_temp
      	  	 	 where fishstr_str_zdb_id = rggt_mrkr_Zdb_id
			 and genox_zdb_id = rggt_genox_zdb_id)
 and not exists (Select 'x' from fish_str b
     	 		    where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
			    and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id);




--    end if

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
