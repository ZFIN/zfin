create procedure regen_names_fish_list()

  -- ---------------------------------------------------------------------
  -- regen_names_fish_list generates all names for the fish identified by 
  -- the ZDB IDs in the regen_zdb_id_temp table and adds the names to the
  -- regen_all_names_temp table.
  --
  -- PRECONDITIONS:
  --   regen_zdb_id_temp table exists and contains a list of fish ZDB IDs
  --     to get names for.
  --   regen_all_names_temp table exists.  It may contain data, but it does
  --     not contain any data for fish.
  --
  -- INPUT VARS:
  --   none
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
  --     regen_all_names_temp contains all names for fish that were identified
  --       in regen_zdb_id_temp table.
  --   Error:
  --     regen_all_names_temp may or may not have new data in it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  define namePrecedence like name_precedence.nmprec_precedence;
  define nameSignificance like name_precedence.nmprec_significance;


  -- crank up the parallelism.

  set pdqpriority high;


  -- get locus names.  The locus name code is reproduced in regen_names_locus
  -- and regen_names_marker as well.  IF YOU MODIFY THIS CODE, THEN MODIFY
  -- THOSE ROUTINES AS WELL.

  let namePrecedence = "Locus abbreviation";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select l.abbrev, f.zdb_id, nameSignificance, namePrecedence,
           lower(l.abbrev)
      from fish f, locus l, regen_zdb_id_temp
      where f.locus = l.zdb_id
        and f.zdb_id = rgnz_zdb_id
        and f.name <> l.abbrev;


  let namePrecedence = "Locus Previous name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select dalias_alias, rgnz_zdb_id, nameSignificance, namePrecedence, 
           dalias_alias_lower
      from data_alias, regen_zdb_id_temp, fish
      where dalias_data_zdb_id = fish.locus
        and fish.zdb_id = rgnz_zdb_id;


  let namePrecedence = "Locus name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select name, zdb_id, nameSignificance, namePrecedence, lower(name)
      from fish, regen_zdb_id_temp
      where line_type = "mutant"
        and zdb_id = rgnz_zdb_id;


  -- Get fish names.

  let namePrecedence = "Wildtype name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select name, zdb_id, nameSignificance, namePrecedence, lower(name)
      from fish, regen_zdb_id_temp
      where line_type = "wild type"
        and zdb_id = rgnz_zdb_id;


  let namePrecedence = "Fish name/allele";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select allele, zdb_id, nameSignificance, namePrecedence, lower(allele)
      from fish, regen_zdb_id_temp
      where allele is not NULL
        and zdb_id = rgnz_zdb_id;


  let namePrecedence = "Wildtype abbreviation";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select abbrev, zdb_id, nameSignificance, namePrecedence, lower(abbrev)
      from fish, regen_zdb_id_temp
      where line_type = "wild type"
        and abbrev <> name
        and rgnz_zdb_id = zdb_id;


  let namePrecedence = "Fish Previous name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select dalias_alias, dalias_data_zdb_id, nameSignificance, namePrecedence, 
           dalias_alias_lower
      from data_alias, regen_zdb_id_temp
      where dalias_data_zdb_id = rgnz_zdb_id;


  -- get aliases

  let namePrecedence = "Allele Previous name";
  select nmprec_significance 
    into nameSignificance
    from name_precedence 
    where nmprec_precedence = namePrecedence;

  insert into regen_all_names_temp
      ( rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
        rgnallnm_precedence, rgnallnm_name_lower )
    select dalias_alias, rgnz_zdb_id, nameSignificance, namePrecedence, 
           dalias_alias_lower
      from data_alias aalias, fish f, alteration a, regen_zdb_id_temp
      where aalias.dalias_data_zdb_id = a.zdb_id
        and f.zdb_id = rgnz_zdb_id
	and a.allele = f.allele
	and not exists
	    ( select 'x' 
	        from data_alias falias
		where falias.dalias_data_zdb_id = f.zdb_id
		  and falias.dalias_alias = aalias.dalias_alias );

  -- remove less significant dups

  execute procedure regen_names_drop_dups();

  -- generate all_name_ends.  Takes regen_zdb_id_temp, regen_all_names_temp
  -- as input, adds recs to regen_all_name_ends_temp

  execute procedure regen_name_ends_list();

end procedure;
