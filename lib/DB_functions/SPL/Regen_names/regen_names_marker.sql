create procedure regen_names_marker(zdbId like zdb_active_data.zactvd_zdb_id)

  -- ---------------------------------------------------------------------
  -- regenerates the names in fast search tables for a given marker.
  --
  -- INPUT VARS:
  --   zdbId    ZDB ID of the marker to regenerate names for.
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
  --     Entries for this marker ZDB ID in all_map_names and all_name_parts 
  --       tables have been replaced with current names for the marker.
  --     Several temp tables, used only by regen_names routines, will have 
  --       been created.  All of them will be empty.
  --   Error:
  --     Entries for this marker ZDB ID in all_map_names and all_name_parts
  --       may or may not have been replaced, or may have been dropped.
  --     Various temp table may or may not have been created.  If created,
  --       they may have data in them.
  --     Transaction has not been committed or rolled back.
  -- ---------------------------------------------------------------------


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_zdb_id_temp, regen_all_names_temp, regen_all_name_ends_temp
  execute procedure regen_names_create_temp_tables();

  -- gather names
  insert into regen_zdb_id_temp
      ( rgnz_zdb_id )
    values
      ( zdbId );

  -- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
  --   and regen_all_name_ends_temp
  execute procedure regen_names_marker_list();

  -- Move from temp tables to permanent tables
  execute procedure regen_names_finish();

end procedure;
