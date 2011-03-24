create procedure regen_genofig_clean_exp()
  -- ---------------------------------------------------------------------------------------------
  -- find the clean data and MO data. finding the data in advance avoids exists clauses 
  -- in the main search.
  -- INPUT VARS:
  --   none
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
  --     Clean versions of experiment condition and "not normal' data for phenotype
  --     records are populated into temp tables regen_genofig_clean_exp_with_morph_temp
  --     and regen_genofig_not_normal_temp.
  --   Error:
  --     None or some of the data will be added to the temp tables.
  --     Transaction has not been committed or rolled back.
  -- ---------------------------------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- gather the clean environments with morpholinos
  insert into regen_genofig_clean_exp_with_morph_temp
      ( rgfcx_clean_exp_zdb_id, rgfcx_morph_zdb_id )
  select distinct exp_zdb_id, expcond_mrkr_zdb_id 
  from experiment, experiment_condition xc1, marker
  where exp_zdb_id = xc1.expcond_exp_zdb_id
    and xc1.expcond_mrkr_zdb_id = mrkr_zdb_id
    and not exists (select 'x'
                      from experiment_condition xc2 , condition_data_type
                     where xc1.expcond_exp_zdb_id = xc2.expcond_exp_zdb_id 
                       and xc2.expcond_cdt_zdb_id = cdt_zdb_id
                       and cdt_group != "morpholino");


  -- gather the "not normal" phenotype records
  insert into regen_genofig_not_normal_temp
    (rgfnna_zdb_id,rgfnna_genox_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag)
    select distinct phenox_pk_id,phenox_genox_zdb_id,phenos_entity_1_superterm_zdb_id,phenos_entity_1_subterm_zdb_id,phenos_quality_zdb_id,phenos_tag
      from phenotype_experiment, phenotype_statement
     where phenox_pk_id = phenos_phenox_pk_id
       and phenos_tag != 'normal';

  insert into regen_genofig_not_normal_temp
    (rgfnna_zdb_id,rgfnna_genox_zdb_id,rgfnna_superterm_zdb_id,rgfnna_subterm_zdb_id,rgfnna_quality_zdb_id,rgfnna_tag)
    select distinct phenox_pk_id,phenox_genox_zdb_id,phenos_entity_2_superterm_zdb_id,phenos_entity_2_subterm_zdb_id,phenos_quality_zdb_id,phenos_tag
      from phenotype_experiment, phenotype_statement
     where phenox_pk_id = phenos_phenox_pk_id
       and phenos_tag != 'normal'
       and phenos_entity_2_superterm_zdb_id is not null;

end procedure;
