create procedure regen_names_locus_list()

  -- ---------------------------------------------------------------------
  -- regen_names_locus_list generates all names for the loci identified by 
  -- the ZDB IDs in the regen_zdb_id_temp table and adds the names to the
  -- regen_all_names_temp table.
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp table exists and contains a list of locus ZDB IDs
  --     to get names for.
  --   regen_all_names_temp table exists.  It may contain data, but it does
  --     not contain any data for locii.
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
  --     regen_all_names_temp contains all names for locii that were identified
  --       in regen_zdb_id_temp table.
  --   Error:
  --     regen_all_names_temp may or may not have new data in it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  define namePrecedence like name_precedence.nmprec_precedence;
  define nameSignificance like name_precedence.nmprec_significance;


  -- crank up the parallelism.

  set pdqpriority high;


  -- all of these locus queries are repeated in slightly altered form in
  -- regen_names_fish_list() and regen_names_marker_list().  
  -- IF YOU MODIFY THIS CODE, ALSO MODIFY THE OTHER TWO ROUTINES.

  let namePrecedence = "Locus abbreviation";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select abbrev, zdb_id, nameSignificance, namePrecedence, lower(abbrev)
      from locus, regen_zdb_id_temp
      where zdb_id = rgnz_zdb_id
        and length(abbrev) > 0;  -- eliminates nulls and blanks

  let namePrecedence = "Locus name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select locus_name, zdb_id, nameSignificance, namePrecedence, 
           lower(locus_name)
      from locus, regen_zdb_id_temp
      where zdb_id = rgnz_zdb_id;

  let namePrecedence = "Locus Previous name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
	rgnallnm_precedence, rgnallnm_name_lower )
    select dalias_alias, dalias_data_zdb_id, nameSignificance, namePrecedence,
	   lower(dalias_alias)
      from data_alias, regen_zdb_id_temp
      where dalias_data_zdb_id = rgnz_zdb_id;

  -- remove less significant dups

  execute procedure regen_names_drop_dups();

  -- generate all name ends.  Takes regen_zdb_id_temp, regen_all_names_temp
  -- as input, adds recs to regen_all_name_ends_temp

  execute procedure regen_name_ends_list();


end procedure;
