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
  -- INPUT VARS
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: Nothing
  --   Failure: Throws whatever exception happened.
  --
  -- EFFECTS:
  --   Success:
  --     records in genotype_figure_fast_search table have been 
  --       updated for the input ZDB ID(s)
  --   Error:
  --     records in genotype_figure_fast_search table may or may not
  --       have been updated 
  --     regen_genofig_input_zdb_id_temp may or may not be empty.
  --     regen_genofig_temp may or may not be empty.
  --     transaction is not committed or rolled back.
  -- -------------------------------------------------------------------------------------------

  delete from genotype_figure_fast_search_new;

  insert into genotype_figure_fast_search_new
      (gffs_geno_zdb_id,gffs_fig_zdb_id,gffs_superterm_zdb_id,gffs_subterm_zdb_id,gffs_quality_zdb_id,gffs_tag,gffs_morph_zdb_id)
    select rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag,rgf_morph_zdb_id
      from regen_genofig_temp;
     
  delete from regen_genofig_temp;
  delete from regen_genofig_clean_exp_with_morph_temp;
  delete from regen_genofig_not_normal_apato_temp;

  delete from regen_genofig_input_zdb_id_temp;

end procedure;
