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

--  foreach
--    select rgfg_zdb_id
--      into rgfg_zdb_id
--      from regen_genofig_input_zdb_id_temp

-- Any genotype which has a morpholino environment
insert into regen_genofig_temp (rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag,rgf_morph_zdb_id,rgf_phenox_pk_id)
  select distinct rgfg_zdb_id,phenox_fig_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag,rgfcx_morph_zdb_id,phenox_pk_id
    from genotype_experiment, 
         regen_genofig_clean_exp_with_morph_temp, 
         regen_genofig_not_normal_temp,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where genox_geno_zdb_id = rgfg_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and genox_exp_zdb_id = rgfcx_clean_exp_zdb_id
     and genox_zdb_id = rgfnna_genox_zdb_id
     and phenox_pk_id = rgfnna_zdb_id;

-- Any which has a standard or genetic control environment

insert into regen_genofig_temp (rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag,rgf_phenox_pk_id)
  select distinct rgfg_zdb_id,phenox_fig_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag,phenox_pk_id
    from genotype_experiment, 
         experiment, 
         regen_genofig_not_normal_temp,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where genox_geno_zdb_id = rgfg_zdb_id
     and genox_exp_zdb_id = exp_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and exp_name like '\_%'
     and genox_zdb_id = rgfnna_genox_zdb_id
     and phenox_pk_id = rgfnna_zdb_id;     

--  end foreach  -- foreach record in regen_genox_input_zdb_id_temp

end procedure;
