create procedure regen_genofig_process()

  -- --------------------------------------------------------------------------------------------
  -- Generates Geno / Fig / Term / MO for records in genotype_figure_fast_search table 
  --
  -- PRECONDITONS:
  --   regen_genofig_clean_exp has run successfully. 
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
  --     regen_genofig_temp contains Geno - Fig pairs and related AO/GO term and MO data.
  --   Error:
  --     regen_genofig_temp may or may not have had data added to it.
  --     transaction is not committed or rolled back.
  -- --------------------------------------------------------------------------------------------

  define genoZdbId like zdb_active_data.zactvd_zdb_id;

  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

  end

  foreach
    select rgfg_zdb_id
      into genoZdbId
      from regen_genofig_input_zdb_id_temp


-- Any genotype which has a morpholino environment
insert into regen_genofig_temp (rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag,rgf_morph_zdb_id)
  select distinct genoZdbId,apatofig_fig_zdb_id,rgfnna_apato_superterm_zdb_id,rgfnna_apato_subterm_zdb_id,rgfnna_apato_quality_zdb_id,rgfnna_apato_tag,rgfcx_morph_zdb_id
    from genotype_experiment, 
         regen_genofig_clean_exp_with_morph_temp, 
         regen_genofig_not_normal_apato_temp,
         apato_figure         
   where genox_geno_zdb_id = genoZdbId
     and genox_exp_zdb_id = rgfcx_clean_exp_zdb_id
     and genox_zdb_id = rgfnna_apato_genox_zdb_id
     and apatofig_apato_zdb_id = rgfnna_apato_zdb_id;

-- Any which has a standard or genetic control environment
insert into regen_genofig_temp (rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag)
  select distinct genoZdbId,apatofig_fig_zdb_id,rgfnna_apato_superterm_zdb_id,rgfnna_apato_subterm_zdb_id,rgfnna_apato_quality_zdb_id,rgfnna_apato_tag
    from genotype_experiment, 
         experiment, 
         regen_genofig_not_normal_apato_temp,
         apato_figure         
   where genox_geno_zdb_id = genoZdbId
     and genox_exp_zdb_id = exp_zdb_id
     and exp_name like '\_%'
     and genox_zdb_id = rgfnna_apato_genox_zdb_id
     and apatofig_apato_zdb_id = rgfnna_apato_zdb_id;     

  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
