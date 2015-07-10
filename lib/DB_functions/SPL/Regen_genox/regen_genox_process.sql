create procedure regen_genox_process()

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
  --
  -- RETURNS:
  --   Success: nothing
  --   Failure: throws whatever exception happened.
  -- 
  -- EFFECTS:
  --   Success:
  --     regen_genox_temp contains marker_zdb_id genox_zdb_id pairs
  --   Error:
  --     regen_genox_temp may or may not have had data added to it.
  --     transaction is not committed or rolled back.
  -- --------------------------------------------------------------------------------------------

--  define markerZdbId like zdb_active_data.zactvd_zdb_id;


--  foreach
--    select rggz_zdb_id
--      into markerZdbId
--      from regen_genox_input_zdb_id_temp

   --------------
   -- for gene
   --------------


-- The genotypes include any of the WT lines, which (cannot be with 'Standard' or 'Generic Control' environments),
-- and has MO(s) which target ONLY this gene
insert into regen_genox_temp (rggt_mrkr_zdb_id, rggt_genox_zdb_id)
  select fmrel_mrkr_zdb_id, genox_zdb_id
    from fish, fish_experiment, feature_marker_relationship, genotype_Feature
    where fish_zdb_id = genox_fish_Zdb_id
    and fish_genotype_zdb_id = genofeat_geno_zdb_id
    and genofeat_feature_Zdb_id = fmrel_ftr_zdb_id
    and fish_functional_affected_gene_count = 1
    and get_obj_type(fmrel_mrkr_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR')
    and genox_is_std_or_generic_control = 't'
  union
  select fishstr_str_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
    and get_obj_type(fishstr_str_Zdb_id) in ('GENE','MRPHLNO','TALEN','CRISPR')
 and genox_is_std_or_generic_control = 't'
  union
  select mrel_mrkr_2_zdb_id, genox_zdb_id
    from fish, fish_str, fish_experiment, marker_relationship
    where fish_Zdb_id =fishstr_fish_Zdb_id
    and fish_zdb_id = genox_fish_zdb_id
    and fish_functional_affected_gene_count =1
    and fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
    and get_obj_type(mrel_mrkr_2_Zdb_id) in ('GENE')
 and genox_is_std_or_generic_control = 't'; 
                          ZDB-CDT-131021-2
			  ZDB-CDT-131021-1
			  ZDB-CDT-050127-5
       
--    end if

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
