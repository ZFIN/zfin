create dba function "informix".regen_names() returning integer

  -- ---------------------------------------------------------------------
  -- regen_names creates fast search tables related to the quick search
  -- of name fields.  (In a former life, it generated most of the fast search
  -- tables at ZFIN.)
  --
  -- This routine runs for a long time.  It plows over a lot of data.
  --
  -- It uses the general ZFIN approach for SPL routines that generate 
  -- fast search tables:
  --
  -- o Very good exception handling and debugging support.  SPL can be a 
  --   bear to debug unless you have the infrastructure in place.  See below.
  --
  -- o It does as much work as possible before doing anything that is
  --   visible outside of the script.  This means:
  --   - Script creates the output tables with names used only in this script.
  --   - At end of script, it enters a transaction, drops the existing tables,
  --     renames everything to their real names, and commits.
  --
  -- INPUT VARS:
  --   none.
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   0 - Success
  --   1 - Failed because another copy of the routine is already running.
  --  -1 - Failed for some other reason.  See 
  --       /tmp/regen_names_exception_<!--|DB_NAME|--> for details.
  --
  -- EFFECTS:
  --   Success:
  --     all_map_names and all_name_parts tables have been replaced with new
  --       versions of the tables.  
  --     If any staging tables existed from a previous run of this routine, 
  --       then they will have been dropped.
  --   Error:
  --     If -1 is returned then /tmp/regen_names_exception_<!--|DB_NAME|--> 
  --       will have been written and any staging tables used by this routine 
  --       will exist in whatever state they were in whenthe error occurred.
  --     Any changes made to permanent tables will have been rolled back.  That
  --       is, an error returns means thatthe permanent tables were not changed.
  --
  -- DEBUGGING:
  --
  -- There are several ways to debug this function.
  --
  -- 1. If this function encounters  an exception it writes the exception
  --    number and associated text out to the file 
  --
  --      /tmp/regen_names_exception_<!--|DB_NAME|-->.
  --
  --    This is a great place to start.  The associated text is often the
  --    name of a violated constraint, for example "u279_351".  The first
  --    number in the contraint name (in this case "279") is the table ID
  --    of the table with the violated constraint.  You can find the table
  --    name by looking in the systables table.
  --
  -- 2. Display additional messages to the /tmp/regen_names_exception
  --    file.  See the master exception handler code below for how this
  --    is done.  You might want to add a display message between the
  --    code for each table that is created.
  --
  -- 3. If the previous 2 approaches aren't enough then you can also turn
  --    on tracing.  Tracing produces a large volume of information and
  --    tends to run mind-numbingly slow.
  --
  --    To turn tracing on uncomment the next statement
  --
  -- set debug file to 'debug-regen';
  --
  --    This enables tracing, but doesn't turn it on.  To turn on tracing,
  --    add a "trace on;" before the first piece of code that you suspect
  --    is causing problems.  Add a "trace off;" after the last piece of
  --    code you suspect.

  --    At this point it becomes a narrowing process to figure out exactly
  --    where the problem is.  Let the function run for a while, kill it,
  --    and then look at the trace file.  If things appear OK in the 
  --    trace, move the "trace on;" to be later in the file and then rerun.
  -- ---------------------------------------------------------------------


  -- set standard set of session params

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
 
     -- 101 Genetic feature name               Genotype
     -- 102 Genetic feature abbreviation       Genotype
     -- 103 Genetic feature alias              Genotype
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
    revoke all on all_m_names_new from "public";



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE ALL_NAME_ENDS_NEW (ALL_NAME_ENDS) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- This table contains all possible ending substrings for every name 
    -- in all_map_names, including the full name itself.  This table is 
    -- used to speed up "name contains" searches.  Before this table existed
    -- such queries had to use 'like "%name%"' which forced a linear search
    -- of every name in all_map_names.  With this table, queries can now
    -- use 'like "name%" which will use the index.
    -- 
    -- This table does not contain trailing substrings for accession numbers.
    -- It contains only the whole accession number.
    
    let errorHint = "all_name_ends_new";   
    if (exists ( select * 
                   from systables 
                   where tabname = "all_name_ends_new" )) then
      drop table all_name_ends_new;
    end if

    create table all_name_ends_new
      (
        allnmend_name_end_lower    	varchar(255),
        allnmend_allmapnm_serial_id	int8
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 16384 next size 16384 lock mode page;
    revoke all on all_name_ends_new from "public";



    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   create regen_zdb_id_temp, regen_all_names_temp, regen_all_name_ends_temp
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "create temp tables";
    execute procedure regen_names_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Feature into all_m_names_new
    --  take this part out of the current regen as we do not have 
    --  feature centric search yet. But we leave it in as we might
    --  need it someday.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    --let errorHint = "Feature";
    ---- gather names
    -- insert into regen_zdb_id_temp
    --    ( rgnz_zdb_id )
    --  select feature_zdb_id from feature;

    ---- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
    --execute procedure regen_names_feature_list();

    --delete from regen_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Genotype into all_m_names_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "Genotype";
    -- gather names
    insert into regen_zdb_id_temp
        ( rgnz_zdb_id )
      select geno_zdb_id from genotype;

    -- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
    execute procedure regen_names_genotype_list();

    delete from regen_zdb_id_temp;

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get Marker NAMES and ACCESSION NUMBERS into all_m_names_new.
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "Markers";
    -- gather names
    insert into regen_zdb_id_temp
        ( rgnz_zdb_id )
      select mrkr_zdb_id from marker;

    -- takes regen_zdb_id_temp as input, adds recs to regen_all_names_temp
    execute procedure regen_names_marker_list();

    delete from regen_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "insert into all_m_names_new";
    insert into all_m_names_new 
        ( allmapnm_name, allmapnm_zdb_id, allmapnm_significance,
          allmapnm_precedence, allmapnm_name_lower, allmapnm_serial_id )
      select rgnallnm_name, rgnallnm_zdb_id, rgnallnm_significance,
             rgnallnm_precedence, rgnallnm_name_lower, rgnallnm_serial_id
        from regen_all_names_temp;

    let errorHint = "insert into all_name_ends_new";
    insert into all_name_ends_new
        ( allnmend_name_end_lower, allnmend_allmapnm_serial_id )
      select rgnnmend_name_end_lower, rgnnmend_rgnallnm_serial_id
        from regen_all_name_ends_temp;

    -- Be paranoid and delete everything from the temp tables.  Shouldn't
    -- need to do this, as this routine is called in it's own session
    -- and therefore the temp tables will be dropped when the routine ends.

    delete from regen_all_names_temp;
    delete from regen_all_name_ends_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    -- primary key
    let errorHint = "all_map_names create PK index";
    create unique index all_map_names_primary_key_index_transient
      on all_m_names_new (allmapnm_serial_id)
      fillfactor 100
      in idxdbs1;
    -- alternate key
    let errorHint = "all_map_names create AK index";
    create unique index allmapnm_alternate_key_index_transient
      on all_m_names_new (allmapnm_name, allmapnm_zdb_id)
      fillfactor 100
      in idxdbs1;
    -- foreign keys
    create index allmapnm_zdb_id_index_transient
      on all_m_names_new (allmapnm_zdb_id)
      fillfactor 100
      in idxdbs3;
    create index allmapnm_precedence_index_transient
      on all_m_names_new (allmapnm_precedence)
      fillfactor 100
      in idxdbs3;
    -- other indexes
    create index allmapnm_name_lower_index_transient
      on all_m_names_new (allmapnm_name_lower)
      fillfactor 100
      in idxdbs3;

    update statistics high for table all_m_names_new;


    -- -------------------------------------------------------------------
    --   create indexes for all_name_ends
    -- -------------------------------------------------------------------

    -- primary key
    let errorHint = "all_name_ends primary key index";

    create unique index all_name_ends_primary_key_index_transient
      on all_name_ends_new (allnmend_name_end_lower, allnmend_allmapnm_serial_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "all_name_ends serial id index";
    create index allnmend_allmapnm_serial_id_index_transient
      on all_name_ends_new (allnmend_allmapnm_serial_id)
      fillfactor 100
      in idxdbs2;

    update statistics high for table all_name_ends_new;




    -- --------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
    -- --------------------------------------------------------------------

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.
    --
    begin work;

    begin -- local exception handler dropping, renaming, and constraints

      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

      on exception in (-206, -535)
	-- 206 ignore error when dropping a table that doesn't already exist
        -- 535 ignore error of already in transaction
      end exception with resume;


      -- Now rename our new tables and indexes to have the permanent names.
      -- Also define primary keys and alternate keys.

      -- Note that the exception-handler at the top of this file is still active


      let errorHint = "drop tables ";
      drop table all_name_ends;
      drop table all_map_names;

      let errorHint = "rename tables ";
      rename table all_m_names_new to all_map_names;
      rename table all_name_ends_new to all_name_ends;

      let errorHint = "rename indexes";
      rename index all_map_names_primary_key_index_transient
        to all_map_names_primary_key_index;
      rename index allmapnm_alternate_key_index_transient
        to allmapnm_alternate_key_index;
      rename index allmapnm_zdb_id_index_transient 
        to allmapnm_zdb_id_index;
      rename index allmapnm_precedence_index_transient
        to allmapnm_precedence_index;
      rename index allmapnm_name_lower_index_transient
        to allmapnm_name_lower_index;
      rename index all_name_ends_primary_key_index_transient
        to all_name_ends_primary_key_index;
      rename index allnmend_allmapnm_serial_id_index_transient
        to allnmend_allmapnm_serial_id_index;

      -- define constraints, indexes are defined earlier.

      let errorHint = "all_map_names PK constraint";
      alter table all_map_names add constraint
	primary key (allmapnm_serial_id)
	constraint all_map_names_primary_key;

      let errorHint = "all_map_names AK constraint";
      alter table all_map_names add constraint
	unique (allmapnm_name, allmapnm_zdb_id)
	constraint all_map_names_alternate_key;

      let errorHint = "allmapnm_zdb_id FK constraint";
      alter table all_map_names add constraint
	foreign key (allmapnm_zdb_id)
        references zdb_active_data
        on delete cascade
	constraint allmapnm_zdb_id_foreign_key;

      let errorHint = "allmapnm_precedence FK constraint";
      alter table all_map_names add constraint
	foreign key (allmapnm_precedence)
        references name_precedence
	constraint allmapnm_precedence_foreign_key;

      let errorHint = "all_name_ends PK constraint";
      alter table all_name_ends add constraint
	primary key (allnmend_name_end_lower, allnmend_allmapnm_serial_id)
	constraint all_name_ends_primary_key;

      let errorHint = "all_name_ends FK constraint";
      alter table all_name_ends add constraint
        foreign key (allnmend_allmapnm_serial_id)
        references all_map_names
        on delete cascade
        constraint allnmend_allmapnm_serial_id_foreign_key;

      grant select on all_map_names to "public";
      grant select on all_name_ends to "public";


      --trace off;
    end -- Local exception handler

    commit work;

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
