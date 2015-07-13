create dba function "informix".regen_names() returning integer

  execute procedure set_session_params();


  -- -------------------------------------------------------------------
  --   MASTER EXCEPTION HANDLER
  -- -------------------------------------------------------------------
  begin

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
   
    define namePrecedence like name_precedence.nmprec_precedence;
    define nameSignificance integer;
    define zdbFlagReturn integer;

    -- for the purpose of time testing	
    define timeMsg varchar(50);

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get rid of them, and leave the original tables around

	on exception in (-255, -668, -535)
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	  --  535: OK to get a "Already in transaction" here.  
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
		               ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_names_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_names_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_names_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

        let zdbFlagReturn = release_zdb_flag("regen_names");
	return -1;
      end
    end exception;


    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

    let errorHint = "Grab zdb_flag";
    if grab_zdb_flag("regen_names") <> 0 then
      return 1;
    end if

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE ALL_M_NAMES_NEW (ALL_MAP_NAMES) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the possible names and abbreviations of markers, features,
    -- and genotypes.
    -- coincidentally are all the names that can occur in maps.
    --
    -- We should perhaps split names for markers, features, and genotypes
    --  into 3 separate tables for perfromance reasons, but not today.
    --
    -- Marker names come from marker(and from 
    -- accession numbers in db_link and orthlogue names/abbrevs in orthologue).
    -- Force the names to lower case.  We don't display out of this table,
    --  we only search it.
    -- 
    -- The entries in all_map_names are prioritized based on their 
    -- precedence/signifcance.  Here are the sgnificance and precedence values
    -- for the threee types of names.  This data comes from the name_precedence
    -- table.

     --   1 Current symbol            Marker
     --   2 Current name              Marker
     --   3 Clone name                Marker
     --   4 Marker relations          Marker (not used in regen_names)
     --   5 Previous name             Marker
     --   6 Orthologue                Marker
     --   7 Accession number          Marker
     --   8 Sequence similarity       Marker
     --   9 Clone contains gene       Marker (not used in regen_names)
 
     -- 101 Genomic feature name               Genotype
     -- 102 Genomic feature abbreviation       Genotype
     -- 103 Genomic feature alias              Genotype
     -- 105 Gene symbol                        Genotype 
     -- 106 Gene name                          Genotype
     -- 107 Gene alias                         Genotype
     -- 120 Wildtype name		       Genotype
     -- 121 Genotype alias                     Genotype

    let errorHint = "all_map_names";

    if (exists (select * from systables where tabname = "all_m_names_new")) then
      drop table all_m_names_new;
    end if

    create table all_m_names_new 
      (
	-- ortho_name and mrkr_name are 255 characters long
	-- db_link.acc_num, and all the abbrev 
	-- columns are all 80 characters or less
	allmapnm_name		varchar (255) not null,
	allmapnm_zdb_id		varchar(50) not null,
	allmapnm_significance	integer not null,
	allmapnm_precedence	varchar(80) not null,
	allmapnm_name_lower	varchar(255) not null
		check (allmapnm_name_lower = lower(allmapnm_name)),
        allmapnm_serial_id	serial8 not null 
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 8192 next size 8192 lock mode page;
    

  end -- Global exception handler


  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------

  if release_zdb_flag("regen_names") <> 0 then
    return 1;
  end if

  return 0;

end function;


grant execute on function "informix".regen_names () 
  to "public" as "informix";
