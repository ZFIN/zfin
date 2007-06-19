create procedure regen_names_finish()

  -- ---------------------------------------------------------------------
  -- Finishes the processing for the regen_names_* routines:
  -- 1. Deletes any names in the permanent tables associated with any ZDB ID
  --    in regen_zdb_id_temp.
  -- 2. Inserts into the permanent tables and new names for those ZDB IDs 
  --    from the regen_all_names_temp and regen_all_name_ends_temp tables 
  -- 3. Deletes all records from the 3 temp tables.
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp contains ZDB IDs whose names will be replaced in the
  --     permanent tables.
  --   regen_all_names_temp contains all the names for the ZDB IDs in the
  --     regen_zdb_id_temp table
  --   regen_all_name_ends_temp contains all the name ends for the names in 
  --     the regen_all_names_temp table.
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
  --     Names and name ends in all_map_names and all_name_ends have 
  --       been replaced with the values in regen_all_names_temp and 
  --       regen_all_name_ends_temp
  --     regen_zdb_id_temp is empty.
  --     regen_all_names_temp is empty.
  --     regen_all_name_ends_temp is emtpy.
  --   Error:
  --     Names and name ends may or may not have been replaced in the
  --       regen_all_names_temp and regen_all_name_ends_temp tables.
  --       The old names may have been deleted and not replaced, or 
  --       only partially replaced.
  --     regen_zdb_id_temp may or may not be empty.
  --     regen_all_names_temp may or may not be empty.
  --     regen_all_name_ends_temp may or may not be emtpy.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  delete from all_map_names
    where allmapnm_zdb_id in
          ( select rgnz_zdb_id
              from regen_zdb_id_temp );

  insert into all_map_names
      ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
        allmapnm_precedence, allmapnm_name_lower)
    select rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
           rgnallnm_precedence, rgnallnm_name_lower
      from regen_all_names_temp;

  insert into all_name_ends
      ( allnmend_name_end_lower, allnmend_allmapnm_serial_id )
    select rgnnmend_name_end_lower, allmapnm_serial_id
      from regen_all_name_ends_temp, all_map_names, regen_all_names_temp
      where allmapnm_zdb_id = rgnallnm_zdb_id
        and rgnallnm_serial_id = rgnnmend_rgnallnm_serial_id
        and allmapnm_name = rgnallnm_name;

  delete from regen_zdb_id_temp;
  delete from regen_all_names_temp;
  delete from regen_all_name_ends_temp;
  delete from regen_geno_related_gene_zdb_id_temp;
  delete from regen_geno_related_gene_zdb_id_distinct_temp ;


end procedure;
