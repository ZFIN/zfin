begin work;

set explain on avoid_execute;

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
     and fish_zdb_id = fishstr_Fish_zdb_id
     and exists (Select 'x' from mutant_fast_search
     	 		where mfs_genox_zdb_id = genox_zdb_id);


rollback work;