create procedure regen_genox_finish()

  -- -------------------------------------------------------------------------------------------
  -- Finishes the processing for the regen_genox_* routines:
  -- 1. Deletes any records in the mutant_fast_search table associated with the input ZDB ID(s)
  --    in regen_zdb_id_temp.
  -- 2. Inserts into the mutant_fast_search table the new records for the input ZDB ID(s)
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
  --     marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table have been 
  --       updated for the input ZDB ID(s)
  --   Error:
  --     marker_zdb_id genox_zdb_id pairs for records in mutant_fast_search table may or may not
  --       have been updated 
  --     regen_genox_input_zdb_id_temp may or may not be empty.
  --     regen_genox_temp may or may not be empty.
  --     transaction is not committed or rolled back.
  -- -------------------------------------------------------------------------------------------

  delete from mutant_fast_search
    where mfs_mrkr_zdb_id in
          ( select rggz_zdb_id
              from regen_genox_input_zdb_id_temp );

  insert into mutant_fast_search
      ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
    select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
      from regen_genox_temp;

  delete from regen_genox_temp;
  delete from regen_genox_input_zdb_id_temp;

end procedure;
