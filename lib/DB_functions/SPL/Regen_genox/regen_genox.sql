create dba function regen_genox() returning integer

  -- -------------------------------------------------------------------------
  -- regen_genox creates mutant_fast_search for phenotype data of
  --   markers and MOs; and create genotype_figure_fast_search table 
  --   for phenotype data of genotypes.
  --
  -- This routine runs for only about 1 minute. 
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
  --       /tmp/regen_genox_exception_<!--|DB_NAME|--> for details.
  --
  -- EFFECTS:
  --   Success:
  --     mutant_fast_search table has been replaced with new versions of the table.  
  --     genotype_figure_fast_search table has been replaced with new versions of the table. 
  --       
  --     If any staging tables existed from a previous run of this routine, 
  --       then they will have been dropped.
  --   Error:
  --     If -1 is returned then /tmp/regen_genox_exception_<!--|DB_NAME|--> 
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
  --      /tmp/regen_genox_exception_<!--|DB_NAME|-->.
  --
  --    This is a great place to start.  The associated text is often the
  --    name of a violated constraint, for example "u279_351".  The first
  --    number in the contraint name (in this case "279") is the table ID
  --    of the table with the violated constraint.  You can find the table
  --    name by looking in the systables table.
  --
  -- 2. Display additional messages to the /tmp/regen_genox_exception
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
set debug file to 'trace.out';
  --    This enables tracing, but doesn't turn it on.  To turn on tracing,
  --    add a "trace on;" before the first piece of code that you suspect
  --    is causing problems.  Add a "trace off;" after the last piece of
  --    code you suspect.

  --    At this point it becomes a narrowing process to figure out exactly
  --    where the problem is.  Let the function run for a while, kill it,
  --    and then look at the trace file.  If things appear OK in the 
  --    trace, move the "trace on;" to be later in the file and then rerun.
  -- ---------------------------------------------------------------------

trace on;   
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
   
    -- define namePrecedence like name_precedence.nmprec_precedence;
    -- define nameSignificance integer;
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
			       '" >> /tmp/regen_genox_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_genox_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_genox_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

       -- let zdbFlagReturn = release_zdb_flag("regen_genox");
	return -1;
      end
    end exception;


    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

--    let errorHint = "Grab zdb_flag";
--    if grab_zdb_flag("regen_genox") <> 0 then
--      return 1;
--    end if

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE mutant_fast_search_new (mutant_fast_search) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the gene zdbIDs/MO zdbIDs and their phenotype-data-related 
    --   genox_zdb_id

    let errorHint = "mutant_fast_search";

    if (exists (select * from systables where tabname = "mutant_fast_search_new")) then
      drop table mutant_fast_search_new;
    end if

    create table mutant_fast_search_new 
      (
        mfs_mrkr_zdb_id varchar(50) not null,
        mfs_genox_zdb_id varchar(50) not null
      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    revoke all on mutant_fast_search_new from "public";


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   create regen_genox_input_zdb_id_temp, regen_genox_temp
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "create genox temp tables";
    execute procedure regen_genox_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into mutant_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "mutant_fast_search_new";

    insert into regen_genox_input_zdb_id_temp ( rggz_zdb_id )
      select mrkr_zdb_id from marker where mrkr_type in ("GENE","MRPHLNO");

    -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp
    execute procedure regen_genox_process();

    delete from regen_genox_input_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Create genotype_figure_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "genotype_figure_fast_search";

    if (exists (select * from systables where tabname = "genotype_figure_fast_search_new")) then
      drop table genotype_figure_fast_search_new;
    end if

    create table genotype_figure_fast_search_new 
      (  
        gffs_geno_zdb_id  varchar(50) not null,
        gffs_fig_zdb_id varchar(50) not null,
        gffs_superterm_zdb_id varchar(50) not null,
        gffs_subterm_zdb_id varchar(50),
        gffs_quality_zdb_id varchar(50) not null,
        gffs_tag varchar(25) not null,
        gffs_morph_zdb_id varchar(50),
        gffs_phenox_pk_id int8 not null,
	gffs_date_created DATETIME YEAR TO SECOND 
			  DEFAULT CURRENT YEAR TO SECOND NOT NULL,         
        gffs_serial_id serial8 not null
      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    revoke all on genotype_figure_fast_search_new from "public";


    -- --------------------------------------------------------------------------------------
    -- --------------------------------------------------------------------------------------
    --   create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_temp,
    --          regen_genofig_temp, regen_genofig_input_zdb_id_temp
    -- --------------------------------------------------------------------------------------
    -- --------------------------------------------------------------------------------------

    let errorHint = "create genofig temp tables";
    execute procedure regen_genofig_create_temp_tables();

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into genotype_figure_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "populate regen_genofig_input_zdb_id_temp";

    insert into regen_genofig_input_zdb_id_temp ( rgfg_zdb_id )
      select geno_zdb_id from genotype;

    let errorHint = "find clean experiments";
    execute procedure regen_genofig_clean_exp();
    

    let errorHint = "fill fast search tables";
    execute procedure regen_genofig_process();


    let errorHint = "consolidate fast search tables";
    execute procedure regen_genofig_finish();


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "insert into mutant_fast_search_new";
    insert into mutant_fast_search_new 
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_genox_temp;

    -- Be paranoid and delete everything from the temp tables.  Shouldn't
    -- need to do this, as this routine is called in it's own session
    -- and therefore the temp tables will be dropped when the routine ends.

    delete from regen_genox_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    let errorHint = "mutant_fast_search_new create PK index";
    create unique index mutant_fast_search_primary_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id, mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "mutant_fast_search_new create another index";
    create index mutant_fast_search_mrkr_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "mutant_fast_search_new create the third index";
    create index mutant_fast_search_genox_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    update statistics high for table mutant_fast_search_new;

--trace on;


    let errorHint = "genotype_figure_fast_search_new create PK index";
    create unique index genotype_figure_fast_search_primary_key_index_transient
      on genotype_figure_fast_search_new (gffs_serial_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "genotype_figure_fast_search_new create geno index";
    create index genotype_figure_fast_search_geno_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_geno_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "genotype_figure_fast_search_new create fig index";
    create index genotype_figure_fast_search_fig_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_fig_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "genotype_figure_fast_search_new create morph index";
    create index genotype_figure_fast_search_morph_foreign_key_index_transient
      on genotype_figure_fast_search_new (gffs_morph_zdb_id)
      fillfactor 100
      in idxdbs1;
        
    update statistics high for table genotype_figure_fast_search_new;

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


      -- Now rename our new table and indexes to have the permanent name.
      -- Also define primary keys and foreign keys.

      -- Note that the exception-handler at the top of this file is still active

      let errorHint = "drop mutant_fast_search table ";
      drop table mutant_fast_search;

      let errorHint = "rename table ";
      rename table mutant_fast_search_new to mutant_fast_search;

      let errorHint = "rename indexes";
      rename index mutant_fast_search_primary_key_index_transient
        to mutant_fast_search_primary_key_index;
      rename index mutant_fast_search_mrkr_zdb_id_foreign_key_index_transient
        to mutant_fast_search_mrkr_zdb_id_foreign_key_index;
      rename index mutant_fast_search_genox_zdb_id_foreign_key_index_transient 
        to mutant_fast_search_genox_zdb_id_foreign_key_index;

      -- define constraints, indexes are defined earlier.

      let errorHint = "mutant_fast_search PK constraint";
      alter table mutant_fast_search add constraint
	primary key (mfs_mrkr_zdb_id, mfs_genox_zdb_id)
	constraint mutant_fast_search_primary_key;

      let errorHint = "mfs_mrkr_zdb_id FK constraint";
      alter table mutant_fast_search add constraint
        foreign key (mfs_mrkr_zdb_id)
        references marker 
        on delete cascade 
        constraint mutant_fast_search_mrkr_Zdb_id_foreign_key_odc;
  

      let errorHint = "mfs_genox_zdb_id FK constraint";
      alter table mutant_fast_search add constraint 
        foreign key (mfs_genox_zdb_id)
        references genotype_experiment on delete cascade 
        constraint mutant_fast_search_genox_Zdb_id_foreign_key_odc;

      grant select on mutant_fast_search to "public";


      -- Make changes public for genotype_figure_fast_search_new
      let errorHint = "drop genotype_figure_fast_search table ";
      drop table genotype_figure_fast_search;

      let errorHint = "rename table gffs";
      rename table genotype_figure_fast_search_new to genotype_figure_fast_search;
      
      let errorHint = "rename gffs indexes";
      rename index genotype_figure_fast_search_primary_key_index_transient
        to genotype_figure_fast_search_primary_key_index;
      rename index genotype_figure_fast_search_geno_foreign_key_index_transient
        to genotype_figure_fast_search_geno_zdb_id_foreign_key_index;
      rename index genotype_figure_fast_search_fig_foreign_key_index_transient 
        to genotype_figure_fast_search_fig_zdb_id_foreign_key_index;
      rename index genotype_figure_fast_search_morph_foreign_key_index_transient 
        to genotype_figure_fast_search_morph_zdb_id_foreign_key_index;


      let errorHint = "genotype_figure_fast_search add PK";
      alter table genotype_figure_fast_search add constraint primary key 
      (gffs_serial_id) constraint gffs_primary_key ;

      let errorHint = "genotype_figure_fast_search add foreign key to reference genotype";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_geno_zdb_id) references genotype on 
      delete cascade constraint gffs_geno_zdb_id_foreign_key);
    
      let errorHint = "genotype_figure_fast_search add foreign key to reference figure";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_fig_zdb_id) references figure on 
      delete cascade constraint gffs_fig_zdb_id_foreign_key);
    
      let errorHint = "genotype_figure_fast_search add foreign key to reference marker";
      alter table genotype_figure_fast_search add constraint (foreign key (gffs_morph_zdb_id) references marker on 
      delete cascade constraint gffs_morph_zdb_id_foreign_key);

      grant select on genotype_figure_fast_search to "public";

     --trace off;
    end -- Local exception handler

    commit work;

  end -- Global exception handler


  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------

--  if release_zdb_flag("regen_genox") <> 0 then
--    return 1;
--  end if

  return 0;

end function;


grant execute on function regen_genox () to "public";
