create or replace function regen_name_ends_list() 
returns void as $$

  -- ---------------------------------------------------------------------
  -- Generates trailing substrings for all the names in the 
  -- regen_all_names_temp table that are associated with ZDB IDs
  -- in the regen_zdb_id_temp table.
  --
  -- PRECONDITONS:
  --   regen_zdb_id_temp table exists and contains a list of ZDB IDs
  --     to generate trailing substrings for.
  --   regen_all_names_temp table exists
  --   current_all_name_ends_temp table may or may not exist.  If it exists,
  --     it is empty.
  --   most_significant_temp table may or may not exist.  If it exists,
  --     it is empty.
  --
  -- INPUT VARS:
  --   none
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Failure: throws whatever exception happened.
  -- 
  -- EFFECTS:
  --   Success:
  --     regen_all_name_ends_temp contains trailing substrings for all the
  --       names in regen_all_names_temp that are associated with ZDB IDs
  --       in the regen_zdb_id_temp table.  Only the most significant 
  --       name ends are kept.  Accession numbers are not broken up -- they
  --       are stored whole.
  --     current_all_name_ends_temp table exists and is empty.
  --     most_significant_temp table exists and is empty.
  --   Error:
  --     regen_all_name_ends_temp may or may not have had data added to it.
  --     current_all_name_ends_temp table may or may not exist and may have data
  --     most_significant_temp table may or may not exist and may have data
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  -- Process the names in regen_all_name_ends_temp in groups according to their
  -- associated ZDB ID.  The pseudocode is roughly:
  --
  -- for each ZDB ID in regen_all_name_ends_temp associated with a ZDB ID in
  --     the regen_zdb_id_temp table
  --   for each name with that ZDB ID
  --     if name is accession number
  --       store it whole in temp table
  --     else
  --       store almost every possible trailing substring of that name in 
  --         temp table, quite happily storing duplicate substrings
  --     end if
  --   end for
  --   store distinct substrings in regen_all_name_ends_temp table.  Where 
  --     there are duplicates, associate the substring with the most significant
  --     name the substring came from.
  -- end for
  --
  -- In the actual code the 2 for loops are combined into 1 loop 
  -- for performance reasons.
  -- 
  -- I also experimented with first loading all the substrings for all
  -- the ZDB IDs into the temp table, and then doing one big insert
  -- for all ZDB IDs into the temp table.  This turned out not to be
  -- faster and it ran out of sort space.
  --
  -- This code has been carefully tweaked for performance.  Be careful.

  declare nameZdbId zdb_active_data.zactvd_zdb_id%TYPE;
	  prevNameZdbId  zdb_active_data.zactvd_zdb_id%TYPE;
   	  nameLower  varchar(255);
	  nameEnd varchar(255); -- like all_map_names.allmapnm_name_lower;
   	  nameLength integer;
   	  namePrecedence  name_precedence.nmprec_precedence%TYPE;
   	  nameSignificance  name_precedence.nmprec_significance%TYPE;
   	  nameSerialId integer;
   	  startColumn integer;

  begin
  
    vacuum (analyze);

    create temporary table current_all_name_ends_temp 
      (
        current_name_end_lower      varchar(255),
        current_rgnallnm_serial_id  serial8,
        current_significance    integer
      ) ;

    -- Identical substrings for a ZDB ID can be generated from different
    -- names with different significances.  When this happens we want
    -- to associate the substring with the most signigicant name only.
    -- This temp table does that for us.

    create temporary table most_significant_temp
      (
        ms_name_lower           varchar(255),
        ms_significance         integer
      );      

  prevNameZdbId = '';

  for nameLower, nameZdbId, nameLength, nameSerialId, namePrecedence,
	   nameSignificance in
    select rgnallnm_name_lower, rgnallnm_zdb_id, length(rgnallnm_name_lower),
	   rgnallnm_serial_id, rgnallnm_precedence, rgnallnm_significance
      from regen_all_names_temp, regen_zdb_id_temp
      where rgnallnm_zdb_id = rgnz_zdb_id
      order by rgnallnm_zdb_id
    loop

    if prevNameZdbId <> nameZdbId then

      insert into most_significant_temp(ms_name_lower, ms_significance)
        select current_name_end_lower, min(current_significance)
          from current_all_name_ends_temp
          group by current_name_end_lower;

      insert into regen_all_name_ends_temp (rgnnmend_name_end_lower, rgnnmend_rgnallnm_serial_id)
        select distinct current_name_end_lower, current_rgnallnm_serial_id
          from current_all_name_ends_temp, most_significant_temp
          where current_significance = ms_significance
            and ms_name_lower = current_name_end_lower;

       delete from most_significant_temp;
       delete from current_all_name_ends_temp;
    end if;

    if namePrecedence = 'Accession number' then
      -- name is an accession number: take it whole.
      -- must have exact matches on accession numbers
      insert into current_all_name_ends_temp(current_name_end_lower, current_rgnallnm_serial_id, 
	    current_significance)
        values 
	  (nameLower, nameSerialId, nameSignificance);
    else
      -- Break the name into almost every possible trailing substring.
      -- For the string 'ab c', code generates substrings in this order:
      -- 'ab c'
      --  'b c'
      --    'c'
      startColumn = 1;
      while startColumn < nameLength loop
        nameEnd = substr(nameLower, startColumn);
        -- Don't store substrings that start with a space
        if substring(nameEnd,1,1) <> '' then
          insert into current_all_name_ends_temp
              ( current_name_end_lower, current_rgnallnm_serial_id, 
		current_significance )
	    values 
	      ( nameEnd, nameSerialId, nameSignificance );
	end if; -- ignore strings that start with blanks
      end loop;
    end if; -- name is not an accession number.
    prevNameZdbId = nameZdbId;

  end loop;  -- foreach record in all_map_name

  -- Dump substrings for last ZDB ID into temp table.
  -- THIS CODE IS DUPLICATED ABOVE.  MAKE CHANGES IN BOTH PLACES.
  -- See code above for comments.

  insert into most_significant_temp
      ( ms_name_lower, ms_significance )
    select current_name_end_lower, min(current_significance)
      from current_all_name_ends_temp
      group by current_name_end_lower;

  insert into regen_all_name_ends_temp
      ( rgnnmend_name_end_lower, rgnnmend_rgnallnm_serial_id )
    select distinct current_name_end_lower, current_rgnallnm_serial_id
      from current_all_name_ends_temp, most_significant_temp
      where current_significance = ms_significance
        and ms_name_lower = current_name_end_lower;

   delete from most_significant_temp;
   delete from current_all_name_ends_temp;

end;


$$ LANGUAGE plpgsql;
