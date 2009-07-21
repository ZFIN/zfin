create procedure regen_genox_marker(zdbId like zdb_active_data.zactvd_zdb_id)

  -- ---------------------------------------------------------------------------------------------
  -- regenerates the marker_zdb_id genox_zdb_id pairs in fast search tables for a given marker.
  -- here the marker types are restricted to gene and MO
  -- INPUT VARS:
  --   zdbId    ZDB ID of the marker (gene or MO) to regenerate marker_zdb_id genox_zdb_id pairs for.
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
  --     Entries for this marker ZDB ID in mutant_fast_search table have been
  --       replaced with current marker_zdb_id genox_zdb_id pairs for the marker.
  --     Several temp tables, used only by regen_genox routines, will have 
  --       been created.  All of them will be empty.
  --   Error:
  --     Entries for this marker ZDB ID in mutant_fast_search table
  --       may or may not have been replaced, or may have been dropped.
  --     Various temp table may or may not have been created.  If created,
  --       they may have data in them.
  --     Transaction has not been committed or rolled back.
  -- ---------------------------------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_genox_input_zdb_id_temp, regen_genox_temp
  execute procedure regen_genox_create_temp_tables();

  -- gather the marker zdbIDs to be processed
  insert into regen_genox_input_zdb_id_temp
      ( rggz_zdb_id )
    values
      ( zdbId );

  -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp
  execute procedure regen_genox_process();

  -- Move from temp tables to permanent tables
  execute procedure regen_genox_finish();

end procedure;
