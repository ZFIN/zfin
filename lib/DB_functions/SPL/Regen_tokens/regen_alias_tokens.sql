create dba function "informix".regen_alias_tokens() returning integer

  -- ---------------------------------------------------------------------
  -- regen_alias_tokens creates the all_alias_tokens table, which
  -- contains all possible names for aliases broken up into tokens.
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
  --       /tmp/regen_alias_tokens_exception_<!--|DB_NAME|--> for details.
  --
  -- EFFECTS:
  --   Success:
  --     all_alias_tokens table has been replaced with a new
  --       version of the table.  
  --     If any staging tables existed from a previous run of this routine, 
  --       then they will have been dropped.
  --   Error:
  --     If -1 is returned then 
  --         /tmp/regen_alias_tokens_exception_<!--|DB_NAME|--> 
  --       will have been written and any staging tables used by this routine 
  --       will exist in whatever state they were in when the error occurred.
  --     Any changes made to permanent tables will have been rolled back.  That
  --       is, an error returns means the permanent table was not changed.
  --
  -- DEBUGGING:
  --
  -- There are several ways to debug this function.
  --
  -- 1. If this function encounters  an exception it writes the exception
  --    number and associated text out to the file 
  --
  --      /tmp/regen_alias_tokens_exception_<!--|DB_NAME|-->.
  --
  --    This is a great place to start.  The associated text is often the
  --    name of a violated constraint, for example "u279_351".  The first
  --    number in the contraint name (in this case "279") is the table ID
  --    of the table with the violated constraint.  You can find the table
  --    name by looking in the systables table.
  --
  -- 2. Display additional messages to the /tmp/regen_alias_tokens_exception
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


  -- set ZFIN standard session params

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
   
    define zdbFlagReturn integer;

    -- for the purpose of time testing	
    define timeMsg varchar(50);

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get rid of them, and leave the original tables around

	on exception in (-255, -668)
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 
          'echo "' || CURRENT ||
	  ' SQL Error: '  || sqlError::varchar(200) || 
	  ' ISAM Error: ' || isamError::varchar(200) ||
	  ' ErrorText: '  || errorText || 
	  ' ErrorHint: '  || errorHint ||
	  '" >> /tmp/regen_alias_tokens_exception_<!--|DB_NAME|-->';

	system exceptionMessage;

	-- Change the mode of the regen_alias_tokens_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_alias_tokens_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

        let zdbFlagReturn = release_zdb_flag("regen_alias_tokens");
	return -1;
      end
    end exception;


    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

    let errorHint = "Grab zdb_flag";
    if grab_zdb_flag("regen_alias_tokens") <> 0 then
      return 1;
    end if



    -- -------------------------------------------------------------------
    --   CREATE ALL_ALIAS_TOKENS TABLE
    -- -------------------------------------------------------------------
    -- Contains all tokens for all alias names

    let errorHint = "Table creation";

    if exists (select * 
                 from systables 
                 where tabname = "all_alias_tokens_new") then
      drop table all_alias_tokens_new;
    end if

    create table all_alias_tokens_new
      (
        aliastok_token_lower      varchar(255) not null,
        check (aliastok_token_lower = lower(aliastok_token_lower)),
        aliastok_dalias_zdb_id  varchar(50) not null
      )
      fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3  
      extent size 128 next size 128 lock mode page;
    revoke all on all_alias_tokens_new from "public";



    -- -------------------------------------------------------------------
    --   create temp tables used by tokenize
    -- -------------------------------------------------------------------

    let errorHint = "create tokenize temp tables";
    execute procedure tokenize_init();



    -- -------------------------------------------------------------------
    --   Populate tokenize input table with all possible alias names
    -- ------------------------------------------------------------------

    let errorHint = "Alias names";
    insert into tokenize_in_temp
        ( tokin_zdb_id, tokin_name )
      select dalias_zdb_id, dalias_alias_lower
        from data_alias, alias_group 
	where dalias_group_id = aliasgrp_pk_id
	and alias_group = "alias";


    -- -------------------------------------------------------------------
    --   Tokenize them into output table.
    -- ------------------------------------------------------------------

    let errorHint = "Tokenize";
    execute procedure tokenize_list();


    -- -------------------------------------------------------------------
    --   Move tokens from temp table to permanent table
    -- ------------------------------------------------------------------

    let errorHint = "Moving tokens to new table";
    insert into all_alias_tokens_new
        ( aliastok_dalias_zdb_id, aliastok_token_lower )
      select distinct tokout_zdb_id, tokout_token
        from tokenize_out_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    -- primary key
    let errorHint = "all_alias_tokens create PK index";

    create unique index all_alias_tokens_primary_key_index_transient
      on all_alias_tokens_new (aliastok_token_lower, aliastok_dalias_zdb_id)
      fillfactor 100
      in idxdbs3;


    -- foreign key
    let errorHint = "create aliastok_dalias_zdb_id FK index";
    create index aliastok_dalias_zdb_id_index_transient
      on all_alias_tokens_new (aliastok_dalias_zdb_id)
      fillfactor 100
      in idxdbs3;

    update statistics high for table all_alias_tokens_new;



    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
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

      on exception in (-206)
	-- ignore error when dropping a table that doesn't already exist
      end exception with resume;


      -- Now rename our new tables and indexes to have the permanent names.
      -- Also define primary keys and alternate keys.

      -- Note that the exception-handler at the top of this file is still active


      let errorHint = "drop table";
      drop table all_alias_tokens;

      let errorHint = "rename table";
      rename table all_alias_tokens_new to all_alias_tokens;
 
      let errorHint = "rename index";
      rename index all_alias_tokens_primary_key_index_transient
        to all_alias_tokens_primary_key_index;
       rename index aliastok_dalias_zdb_id_index_transient 
        to aliastok_dalias_zdb_id_index;


      -- define constraints

      let errorHint = "all_alias_tokens PK constraint";
      alter table all_alias_tokens add constraint
	primary key (aliastok_token_lower, aliastok_dalias_zdb_id)
	constraint all_alias_tokens_primary_key;

      let errorHint = "aliastok_dalias_zdb_id FK constraint";
      alter table all_alias_tokens add constraint
	foreign key (aliastok_dalias_zdb_id)
        references zdb_active_data
        on delete cascade
	constraint aliastok_dalias_zdb_id_foreign_key;

      let errorHint = "grant select";
      grant select on all_alias_tokens to "public";


      --trace off;
    end -- Local exception handler

    commit work;

  end -- Global exception handler


  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------

  if release_zdb_flag("regen_alias_tokens") <> 0 then
    return 1;
  end if

  return 0;

end function;


grant execute on function "informix".regen_alias_tokens () 
  to "public" as "informix";
