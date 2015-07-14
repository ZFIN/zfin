create procedure regen_genofig_finish()

  -- -------------------------------------------------------------------------------------------
  -- Finishes the processing for the regen_genox_* routines:
  -- 1. Deletes any records in the genotype_figure_fast_search table associated with the input ZDB ID(s)
  --    in regen_genofig_temp.
  -- 2. Inserts into the genotype_figure_fast_search table the new records for the input ZDB ID(s)
  -- 3. Deletes all records from the temp tables.
  --
  -- PRECONDITIONS:
  --   regen_genofig_input_zdb_id_temp table exists and contains a list of genotype ZDB IDs
  --     to generate records in genotype_figure_fast_search table
  --
  -- -------------------------------------------------------------------------------------------
  delete from genotype_figure_fast_search
   where exists (Select 'x' from regen_genofig_input_zdb_id_temp
   	 		where gffs_phenos_id = rgfg_id);

  insert into genotype_figure_fast_search_new
      (gffs_geno_zdb_id,
	gffs_fig_zdb_id,
	gffs_morph_zdb_id,
	gffs_phenox_pk_id,
	gffs_fish_zdb_id,
	gffs_phenos_id,
	gffs_genox_Zdb_id)
    select distinct rgf_geno_zdb_id,
    	   rgf_fig_zdb_id,
	   rgf_morph_zdb_id,
	   rgf_phenox_pk_id,
	   rgf_fish_zdb_id,
	   rgf_phenos_id,
	   rgf_genox_zdb_id
      from regen_genofig_temp;
     
  delete from regen_genofig_temp;
  delete from regen_genofig_input_zdb_id_temp;

end procedure;
