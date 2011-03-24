create procedure regen_genofig_geno_finish(phenoxId like phenotype_experiment.phenox_pk_id)

  -- -------------------------------------------------------------------------------------------
  -- Finishes the processing for the regen_genofig_genotype(for a given phenoxId) routines:
  -- 1. Deletes any records in the genotype_figure_fast_search table associated with the input phenox id
  --    in regen_zdb_id_temp.
  -- 2. Inserts into the genotype_figure_fast_search table the new records for the input phenox id
  -- 3. Deletes all records from the temp tables.
  --
  -- PRECONDITIONS:
  --   regen_genox_input_zdb_id_temp table exists and contains a list of gene and/or MO ZDB IDs
  --     to generate marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table
  --   regen_genox_temp table contains marker_zdb_id genox_zdb_id pairs
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
  --       updated for the input phenox id
  --   Error:
  --     records in mutant_fast_search table may or may not
  --       have been updated 
  --     regen_genox_input_zdb_id_temp may or may not be empty.
  --     regen_genox_temp may or may not be empty.
  --     transaction is not committed or rolled back.
  -- -------------------------------------------------------------------------------------------

  -- remove all records for given phenox id
  delete from genotype_figure_fast_search
    where gffs_phenox_pk_id = phenoxId;

  insert into genotype_figure_fast_search
      (gffs_geno_zdb_id,gffs_fig_zdb_id,gffs_superterm_zdb_id,gffs_subterm_zdb_id,gffs_quality_zdb_id,gffs_tag,gffs_morph_zdb_id, gffs_Phenox_pk_id )
    select rgf_geno_zdb_id,rgf_fig_zdb_id,rgf_superterm_zdb_id,rgf_subterm_zdb_id,rgf_quality_zdb_id,rgf_tag,rgf_morph_zdb_id, rgf_phenox_pk_id
      from regen_genofig_temp;

  delete from regen_genofig_temp;
  delete from regen_genofig_clean_exp_with_morph_temp;
  delete from regen_genofig_not_normal_temp;

  delete from regen_genofig_input_zdb_id_temp;

end procedure;
