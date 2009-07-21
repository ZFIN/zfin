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
  --     Clean versions of experiment condition and "not normal' data for apato
  --     records are populated into temp tables regen_genofig_clean_exp_with_morph_temp
  --     and regen_genofig_not_normal_apato_temp.
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


  -- gather the "not normal" apato records
  insert into regen_genofig_not_normal_apato_temp
    (rgfnna_apato_zdb_id, rgfnna_apato_genox_zdb_id, rgfnna_apato_superterm_zdb_id, rgfnna_apato_subterm_zdb_id)
    select distinct apato_zdb_id, apato_genox_zdb_id, apato_superterm_zdb_id, apato_subterm_zdb_id
      from atomic_phenotype
     where apato_tag != 'normal';

end procedure;
