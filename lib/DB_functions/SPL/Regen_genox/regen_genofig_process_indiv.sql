create procedure regen_genofig_process_indiv(phenoxId like phenotype_experiment.phenox_pk_id)

  -- --------------------------------------------------------------------------------------------
  -- Generates Geno / Fig / Term / MO for records in genotype_figure_fast_search table 
  --
  -- PRECONDITONS:
  --   regen_genofig_clean_exp has run successfully. 
  --
  -- --------------------------------------------------------------------------------------------


-- Any genotype which has a morpholino environment
insert into regen_genofig_temp (rgf_geno_zdb_id,
				rgf_fig_zdb_id,
				rgf_morph_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_zdb_id,	
				rgf_phenos_id,
				rgf_genox_Zdb_id)
  select distinct geno_zdb_id,
  	 	  phenox_fig_zdb_id,
		  rgfcx_morph_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_zdb_id
    from fish_experiment, 
    	 genotype, 
         regen_genofig_clean_exp_with_morph_temp, 
         regen_genofig_not_normal_temp,
         phenotype_statement,
         phenotype_experiment, fish
   where genox_fish_zdb_id = fish_zdb_id
     and fish_genotype_zdb_id = geno_zdb_id
     and phenox_pk_id = phenoxId
     and phenox_genox_zdb_id = genox_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and genox_exp_zdb_id = rgfcx_clean_exp_zdb_id
     and genox_zdb_id = rgfnna_genox_zdb_id
     and phenox_pk_id = rgfnna_zdb_id;

-- Any which has a standard or genetic control environment
insert into regen_genofig_temp (rgf_geno_zdb_id,
       	    		        rgf_fig_zdb_id,
				rgf_phenox_pk_id,
				rgf_fish_Zdb_id,
				rgf_phenos_id,
				rgf_genox_Zdb_id)
  select distinct geno_zdb_id,
  	 	  phenox_fig_zdb_id,
		  phenox_pk_id,
		  fish_zdb_id,
		  phenos_pk_id,
		  genox_Zdb_id
    from fish_experiment, 
    	 genotype,
         experiment, 
         regen_genofig_not_normal_temp,
         phenotype_statement,
         phenotype_experiment, fish
   where genox_fish_zdb_id = fish_zdb_id
     and fish_genotype_zdb_id = geno_zdb_id
     and phenox_pk_id = phenoxId
     and phenox_genox_zdb_id = genox_zdb_id
     and genox_exp_zdb_id = exp_zdb_id
     and phenox_pk_id = phenos_phenox_pk_id
     and exp_name like '\_%'
     and genox_zdb_id = rgfnna_genox_zdb_id
     and phenox_pk_id = rgfnna_zdb_id;     

end procedure;
