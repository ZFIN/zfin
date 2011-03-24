create procedure regen_genofig_genotype(phenoxId like phenotype_experiment.phenox_pk_id)

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
  --
  -- EFFECTS:
  --   Success:
  --     Entries for those phenotype experiments in genotype_figure_fast_search table have been
  --       replaced.
  --     Several temp tables, used only by regen_genofig routines, will have 
  --       been created.  All of them will be empty.
  --   Error:
  --     Entries for this phenox id in genotype_figure_fast_search table
  --       may or may not have been replaced, or may have been dropped.
  --     Various temp table may or may not have been created.  If created,
  --       they may have data in them.
  --     Transaction has not been committed or rolled back.
  -- ---------------------------------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_temp,
  --        regen_genofig_temp, regen_genofig_input_zdb_id_temp
  execute procedure regen_genofig_create_temp_tables();

  -- gather the clean environments with morpholinos
  insert into regen_genofig_clean_exp_with_morph_temp
      ( rgfcx_clean_exp_zdb_id, rgfcx_morph_zdb_id )
  select distinct genox_exp_zdb_id, expcond_mrkr_zdb_id 
    from genotype_experiment, experiment_condition xc1, marker, 
         phenotype_experiment, genotype
   where phenox_pk_id = phenoxId
     and genox_zdb_id = phenox_genox_zdb_id
     and genox_geno_zdb_id = geno_zdb_id
     and genox_exp_zdb_id = xc1.expcond_exp_zdb_id
     and xc1.expcond_mrkr_zdb_id = mrkr_zdb_id
     and not exists (select 'x' 
                       from experiment_condition xc2 , condition_data_type
                      where xc1.expcond_exp_zdb_id = xc2.expcond_exp_zdb_id 
                        and xc2.expcond_cdt_zdb_id = cdt_zdb_id
                        and cdt_group != "morpholino");


  -- gather the "not normal" phenotype recordsyes
  insert into regen_genofig_not_normal_temp
      (rgfnna_zdb_id,rgfnna_genox_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag)
  select distinct phenox_pk_id,phenox_genox_zdb_id,phenos_entity_1_superterm_zdb_id,phenos_entity_1_subterm_zdb_id,phenos_quality_zdb_id,phenos_tag
    from phenotype_experiment, phenotype_statement, genotype_experiment
   where phenox_pk_id = phenoxId
     and phenox_genox_zdb_id = genox_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal';

  insert into regen_genofig_not_normal_temp
      (rgfnna_zdb_id,rgfnna_genox_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag)
  select distinct phenox_pk_id,phenox_genox_zdb_id,phenos_entity_2_superterm_zdb_id,phenos_entity_2_subterm_zdb_id,phenos_quality_zdb_id,phenos_tag
    from phenotype_experiment, phenotype_statement, genotype_experiment
   where phenox_pk_id = phenoxId
     and phenox_genox_zdb_id = genox_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenos_tag != 'normal'
     and phenos_entity_2_superterm_zdb_id is not null;

  -- takes regen_genofig_input_zdb_id_temp as input, adds recs to regen_genofig_temp
  execute procedure regen_genofig_process_indiv(phenoxId);

  -- Move from temp tables to permanent tables
  execute procedure regen_genofig_geno_finish(phenoxId);

end procedure;
