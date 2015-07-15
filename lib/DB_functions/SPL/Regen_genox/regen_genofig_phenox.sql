create procedure regen_genofig_phenox(phenoxId like phenotype_Experiment.phenox_pk_id, genoxId like fish_Experiment.genox_zdb_id)

  -- ---------------------------------------------------------------------------------------------
  -- regenerates records in fast search table genotype_figure_fast_search for a given genotype.
  -- INPUT VARS:
  --   phenoxId    PK id of phenotype_experiment
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Error:   throws whatever exception occurred.
  -- ---------------------------------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_temp,
  --        regen_genofig_temp, regen_genofig_input_zdb_id_temp
  execute procedure regen_genofig_create_temp_tables();

  execute procedure regen_genox_genox(genoxId);
  -- takes regen_genofig_input_zdb_id_temp as input, adds recs to regen_genofig_temp

  insert into  regen_genofig_input_zdb_id_temp ( rgfg_id )
      select phenox_pk_id from phenotype_experiment
        where phenox_pk_id = phenoxId;

  execute procedure regen_genofig_process();

  -- Move from temp tables to permanent tables
  execute procedure regen_genofig_finish();

end procedure;
