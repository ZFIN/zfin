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

  -- gather the clean environments with sequence-targeting reagents

  insert into regen_genofig_clean_exp_with_morph_temp
      ( rgfcx_clean_exp_zdb_id, rgfcx_morph_zdb_id )
  select distinct genox_exp_zdb_id, fishstr_str_zdb_id
    from fish_experiment, marker, 
         phenotype_experiment, genotype, fish_str, fish
   where genox_zdb_id = phenox_genox_zdb_id
     and fish_genotype_zdb_id = geno_zdb_id
     and fish_zdb_id = genox_fish_zdb_id
     and not exists (select 'x' 
                       from experiment_condition xc2 , condition_data_type
                      where genox_exp_zdb_id = xc2.expcond_exp_zdb_id
                        and xc2.expcond_cdt_zdb_id = cdt_zdb_id
                        and cdt_group not in ("morpholino","TALEN","CRISPR"));


  -- gather the "not normal" phenotype records
  insert into regen_genofig_not_normal_temp
    (rgfnna_zdb_id,
	rgfnna_genox_zdb_id,
	rgfnna_phenos_id
)
    select distinct phenox_pk_id,
    	   	    phenox_genox_zdb_id,
		    phenos_pk_id
      from phenotype_experiment, phenotype_statement
     where phenox_pk_id = phenos_phenox_pk_id
       and phenos_tag != 'normal';

end procedure;
