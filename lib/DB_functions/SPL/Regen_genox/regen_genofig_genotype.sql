create procedure regen_genofig_genotype(zdbId like zdb_active_data.zactvd_zdb_id)

  -- ---------------------------------------------------------------------------------------------
  -- regenerates records in fast search table genotype_figure_fast_search for a given genotype.
  -- INPUT VARS:
  --   zdbId    ZDB ID of the genotype 
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Error:   throws whatever exception occurred.
  --
  -- EFFECTS:
  --   Success:
  --     Entries for this genotype ZDB ID in genotype_figure_fast_search table have been
  --       replaced.
  --     Several temp tables, used only by regen_genofig routines, will have 
  --       been created.  All of them will be empty.
  --   Error:
  --     Entries for this genotype ZDB ID in genotype_figure_fast_search table
  --       may or may not have been replaced, or may have been dropped.
  --     Various temp table may or may not have been created.  If created,
  --       they may have data in them.
  --     Transaction has not been committed or rolled back.
  -- ---------------------------------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_apato_temp,
  --        regen_genofig_temp, regen_genofig_input_zdb_id_temp
  execute procedure regen_genofig_create_temp_tables();

  -- gather the genotype zdbIDs to be processed
  insert into regen_genofig_input_zdb_id_temp
      ( rgfg_zdb_id )
    values
      ( zdbId );

  -- gather the clean environments with morpholinos
  insert into regen_genofig_clean_exp_with_morph_temp
      ( rgfcx_clean_exp_zdb_id, rgfcx_morph_zdb_id )
  select distinct genox_exp_zdb_id, expcond_mrkr_zdb_id 
    from genotype_experiment, experiment_condition xc1, marker
   where genox_geno_zdb_id = zdbId
     and genox_exp_zdb_id = xc1.expcond_exp_zdb_id
     and xc1.expcond_mrkr_zdb_id = mrkr_zdb_id
     and not exists (select 'x' 
                       from experiment_condition xc2 , condition_data_type
                      where xc1.expcond_exp_zdb_id = xc2.expcond_exp_zdb_id 
                        and xc2.expcond_cdt_zdb_id = cdt_zdb_id
                        and cdt_group != "morpholino");


  -- gather the "not normal" apato records
  insert into regen_genofig_not_normal_apato_temp
      (rgfnna_apato_zdb_id,rgfnna_apato_genox_zdb_id,rgfnna_apato_superterm_zdb_id,rgfnna_apato_subterm_zdb_id,rgfnna_apato_quality_zdb_id,rgfnna_apato_tag)
  select distinct apato_zdb_id,apato_genox_zdb_id,apato_superterm_zdb_id,apato_subterm_zdb_id,apato_quality_zdb_id,apato_tag
    from atomic_phenotype, genotype_experiment
   where genox_geno_zdb_id = zdbId
     and apato_genox_zdb_id = genox_zdb_id
     and apato_tag != 'normal';

  -- takes regen_genofig_input_zdb_id_temp as input, adds recs to regen_genofig_temp
  execute procedure regen_genofig_process();

  -- Move from temp tables to permanent tables
  execute procedure regen_genofig_geno_finish();


end procedure;
