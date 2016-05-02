create  function regen_genox() returning integer

  -- -------------------------------------------------------------------------
  -- regen_genox creates mutant_fast_search for phenotype data of
  --   markers and MOs; and create genotype_figure_fast_search table 
  --   for phenotype data of genotypes.
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
  -- ---------------------------------------------------------------------

--trace on;   
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
      select mrkr_zdb_id from marker where mrkr_type in ("GENE","MRPHLNO","TALEN", "CRISPR");

    let errorHint = "insert into mutant_fast_search_new";

    -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp
    execute procedure regen_genox_process_marker();

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
  
   let errorHint = "add any old mfs records in the case of regening a certain id instead of the entire table.";
   execute procedure regen_genox_finish_marker();


--trace on;
 

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

    insert into regen_genofig_input_zdb_id_temp ( rgfg_id )
      select pg_id from phenotype_source_generated;


    let errorHint = "fill fast search tables genofig";
    execute procedure regen_genofig_process();

    let errorHint = "regen_genofig_finish procedure";
    execute procedure regen_genofig_finish('f','');

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


  end -- Global exception handler


  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------

--  if release_zdb_flag("regen_genox") <> 0 then
--    return 1;
--  end if

  return 0;

end function;
