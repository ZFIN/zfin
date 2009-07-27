create procedure regen_names_drop_dups()

  -- ---------------------------------------------------------------------
  -- drops less significant duplicate names from the regen_all_names_temp
  -- table.  It does this only for names that are associated with ZDB IDs
  -- in the regen_zdb_id_temp table.
  --
  -- PRECONDITONS:
  --   regen_zdb_id_temp table exists and contains a list of ZDB IDs
  --     to drop less significant names for.
  --   regen_all_names_temp table exists, and does not contain names for any
  --     of the ZDB IDs listed in regen_zdb_id_temp.
  --   regen_names_dups_temp table may or may not exist.  If it exists, it
  --     is empty.
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
  --     regen_all_names_temp no longer has duplicate names for any of the 
  --       the ZDB IDs passed in in the regen_zdb_id_temp table.  Only the
  --       most significant name is kept.
  --     regen_names_dups_temp exists and is empty.
  --   Error:
  --     regen_all_names_temp may or may not have had dups removed from it.
  --     regen_names_dups_temp may or may not exist.  If it exists, it may
  --       have records in it.
  --     transaction is not committed or rolled back.
  -- ---------------------------------------------------------------------

  begin
    on exception in (-958, -316)
      -- Ignore these errors:
      --  -958: Temp table already exists.
      --  -316: Index name already exists.
    end exception with resume;

    create temp table regen_names_dups_temp
      ( 
        rgndup_name_lower      varchar(255),
        rgndup_zdb_id          varchar(50),
        rgndup_min_significance integer
      ) with no log;
    create index rgndup_zdb_id_index on regen_names_dups_temp (rgndup_zdb_id);
  end

  insert into regen_names_dups_temp
    select rgnallnm_name_lower, rgnallnm_zdb_id, 
           min(rgnallnm_significance) rgnallnm_significance 
      from regen_all_names_temp, regen_zdb_id_temp
      where rgnallnm_zdb_id = rgnz_zdb_id
      group by rgnallnm_zdb_id, rgnallnm_name_lower
      having count(*) > 1;

-- do not remove entries where marker is an EST with  EST name and accession number are identical 
-- example DQ017726  
-- change made for fogbugz case 3026

  delete from regen_all_names_temp
    where exists (
              select 'x'
                from regen_names_dups_temp
		where rgndup_name_lower       = rgnallnm_name_lower
		and   rgndup_zdb_id           = rgnallnm_zdb_id
		and   rgndup_min_significance < rgnallnm_significance
		and   NOT (rgnallnm_zdb_id[1,8] = 'ZDB-EST-' and rgnallnm_significance = 7)); 
 

  delete from regen_names_dups_temp;

end procedure;

