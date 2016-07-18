create function regen_clean_expression() returning integer

  -- -------------------------------------------------------------------------
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
  --       /tmp/regen_cleanExpression_exception_<!--|DB_NAME|--> for details.
  --

--trace on;   
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
    define timeMsg varchar(50);

    on exception
      set sqlError, isamError, errorText
      begin

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
			       '" >> /tmp/regen_cleanExpression_exception_<!--|DB_NAME|-->';
	system exceptionMessage;
	system '/bin/chmod 666 /tmp/regen_cleanExpression_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;
	return -1;
      end
    end exception;

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE clean_expression_fast_search_new (clean_expression_fast_search) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the gene zdbIDs/MO zdbIDs and their expression-data-related 
    --   genox_zdb_id

    let errorHint = "clean_expression_fast_search";

    if (exists (select * from systables where tabname = "clean_expression_fast_search_new")) then
      drop table clean_expression_fast_search_new;
    end if

    create table clean_expression_fast_search_new 
      ( cefs_pk_id serial8 not null,
        cefs_mrkr_zdb_id varchar(50) not null,
        cefs_genox_zdb_id varchar(50) not null
      )
    fragment by round robin in tbldbs1, tbldbs2, tbldbs3
    extent size 512 next size 512 ;
    


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   create regen_cleanExpression_input_zdb_id_temp, regen_cleanExpression_temp
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "create ce temp tables";
    execute procedure regen_clean_expression_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into clean_expression_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "clean_expression_fast_search_new";

     insert into regen_ce_input_zdb_id_temp ( rggz_mrkr_zdb_id, rggz_genox_zdb_id )
      select mrkr_zdb_id, genox_Zdb_id from marker, expression_Experiment, fish_experiment 
       where mrkr_type in ("GENE","GENEP")
        and  xpatex_gene_Zdb_id = mrkr_Zdb_id
			   and xpatex_genox_zdb_id = genox_zdb_id
			   and (genox_is_std_or_generic_control = 't' or not exists (Select 'x' from experiment_condition
			       					       	   	  	  where genox_exp_Zdb_id =expcond_Exp_zdb_id))
  	and not exists (Select 'x' from fish_str
			       where genox_fish_zdb_id = fishstr_fish_Zdb_id) ;



    insert into regen_ce_input_zdb_id_temp ( rggz_mrkr_zdb_id, rggz_genox_zdb_id )
      select mrkr_zdb_id, genox_Zdb_id from marker,fish_str, fish_experiment, expression_experiment 
        where mrkr_type in ("MRPHLNO","CRISPR","TALEN")
          and fishstr_fish_Zdb_id = genox_fish_zdb_id
	  and genox_zdb_id = xpatex_genox_zdb_id
          and mrkr_Zdb_id = fishstr_Str_zdb_id
	  and (genox_is_std_or_generic_control = 't' or not exists (Select 'x' from experiment_condition
			       					       	      where genox_exp_Zdb_id =expcond_Exp_zdb_id));
  
 

let errorHint = "clean_expression_fast_search_new_proc";
    -- takes regen_cleanExpression_input_zdb_id_temp as input, adds recs to regen_cleanExpression_temp
    execute procedure regen_clean_expression_process();

    delete from regen_ce_input_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    let errorHint = "insert into clean_expression_fast_search_new";
    insert into clean_expression_fast_search_new 
        ( cefs_mrkr_zdb_id, cefs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_ce_temp;

    -- Be paranoid and delete everything from the temp tables.  Shouldn't
    -- need to do this, as this routine is called in it's own session
    -- and therefore the temp tables will be dropped when the routine ends.

    delete from regen_ce_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

    let errorHint = "clean_expression_fast_search_new create PK index";
    create unique index clean_expression_fast_search_primary_key_index_transient
      on clean_expression_fast_search_new (cefs_mrkr_zdb_id, cefs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "clean_expression_fast_search_new create another index";
    create index clean_expression_fast_search_mrkr_zdb_id_foreign_key_index_transient
      on clean_expression_fast_search_new (cefs_mrkr_zdb_id)
      fillfactor 100
      in idxdbs1;

    let errorHint = "clean_expression_fast_search_new create the third index";
    create index clean_expression_fast_search_genox_zdb_id_foreign_key_index_transient
      on clean_expression_fast_search_new (cefs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    update statistics high for table clean_expression_fast_search_new;


      let errorHint = "drop clean_expression_fast_search table ";
      drop table clean_expression_fast_search;

      let errorHint = "rename table ";
      rename table clean_expression_fast_search_new to clean_expression_fast_search;

      let errorHint = "rename indexes";
      rename index clean_expression_fast_search_primary_key_index_transient
        to clean_expression_fast_search_primary_key_index;
      rename index clean_expression_fast_search_mrkr_zdb_id_foreign_key_index_transient
        to clean_expression_fast_search_mrkr_zdb_id_foreign_key_index;
      rename index clean_expression_fast_search_genox_zdb_id_foreign_key_index_transient 
        to clean_expression_fast_search_genox_zdb_id_foreign_key_index;

      -- define constraints, indexes are defined earlier.

      let errorHint = "clean_expression_fast_search PK constraint";
      alter table clean_expression_fast_search add constraint
	primary key (cefs_mrkr_zdb_id, cefs_genox_zdb_id)
	constraint clean_expression_fast_search_primary_key;

      let errorHint = "cefs_mrkr_zdb_id FK constraint";
      alter table clean_expression_fast_search add constraint
        foreign key (cefs_mrkr_zdb_id)
        references marker 
        on delete cascade 
        constraint clean_expression_fast_search_mrkr_Zdb_id_foreign_key_odc;
  

      let errorHint = "cefs_genox_zdb_id FK constraint";
      alter table clean_expression_fast_search add constraint 
        foreign key (cefs_genox_zdb_id)
        references fish_experiment on delete cascade 
        constraint clean_expression_fast_search_genox_Zdb_id_foreign_key_odc;

  end -- Global exception handler


  return 0;

end function;

