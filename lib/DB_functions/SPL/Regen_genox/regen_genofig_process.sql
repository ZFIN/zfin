create procedure regen_genofig_process()

  -- --------------------------------------------------------------------------------------------
  -- Generates Geno / Fig / Term / MO for records in genotype_figure_fast_search table 
  --
  -- PRECONDITONS:
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none

-- Any fish which has STR(s)

insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_morph_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_Zdb_id,
				rgf_phenos_id,
				rgf_genox_zdb_id)
  select distinct fish_genotype_zdb_id,
  	 	  phenox_fig_zdb_id,
		  fishstr_str_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_Zdb_id
    from fish_experiment, fish, fish_str,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where fish_zdb_id = genox_fish_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and phenox_pk_id = rgfg_id
     and fishstr_fish_zdb_id = fish_Zdb_id
     and phenox_genox_zdb_id = genox_zdb_id
     and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id);

insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_Zdb_id,
				rgf_phenos_id,
				rgf_genox_zdb_id)
  select distinct fish_genotype_zdb_id,
  	 	  phenox_fig_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_Zdb_id
    from fish_experiment, fish,
         phenotype_statement,
         phenotype_experiment,
	 regen_genofig_input_zdb_id_temp
   where fish_zdb_id = genox_fish_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
   and phenox_pk_id = rgfg_id
 and phenox_genox_zdb_id = genox_zdb_id
   and not exists (Select 'x' from fish_Str where fishstr_fish_zdb_id = fish_Zdb_id)
     and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id);

end procedure;
