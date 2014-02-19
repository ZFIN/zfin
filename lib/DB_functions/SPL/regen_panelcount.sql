create dba function "informix".regen_panelcount()
  returning integer

-- Creates the panel_count table, a fast search table used to quickly
-- get counts for each marker type on each linkage group or panel.

-- DEBUGGING:  Uncomment the next two statements to turn on a debugging trace.
--             (and change the first one to point to your OWN dang directory!)
-- set debug file to '/tmp/debug-regen-panelcnt';
-- trace on;

-- Create all the new tables and views.
-- If an exception occurs here, drop all the newly-created tables

  begin	-- master exception handler
        
    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
   
    define nrows integer;	

    on exception
      set sqlError, isamError, errorText
      begin
	-- Something terrible happened while creating the new table
	-- Get rid of it, and leave the original table around.

	on exception in (-206, -255, -668)
	  --  206: OK to get "Table not found" here, since we might
	  --       not have created all tables at the time of the exception
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
                   ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_panelcount_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_panelcount_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_panelcount_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

	return -1;
      end
    end exception;
	

    -- Create a new panelcount table under a temp name

    let errorHint = "create temp table";
	
    if (exists (select *
	          from systables
		  where tabname = "panel_count_new")) then
      drop table panel_count_new;
    end if

    create table panel_count_new
  	(
    	panelcnt_panel_zdb_id	varchar(50),
    	panelcnt_mrkr_type		varchar(10),
    	panelcnt_or_lg		varchar(2),
    	panelcnt_count		integer
      	not null
 	 )
 	 in tbldbs3
  	extent size 32 next size 32
  	lock mode page;
	revoke all on panel_count_new from "public";

     let errorHint = "populate table";

     insert into panel_count_new
          select refcross_id, marker_type, or_lg, count(*)
	    from mapped_marker
        group by refcross_id, or_lg, marker_type;
	

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.

    begin work;

    -- Delete the old table.  It may not exist (if the DB has just
    -- been created), so ignore errors from the drop.

    begin -- local exception handler for dropping of original table

      on exception in (-206)
	-- ignore any table that doesn't already exist
      end exception with resume;
      drop table panel_count;


    end -- local exception handler for dropping of original table

    -- Now rename our new tables to have the permanent names.
    -- Note that the exception-handler at the top of this file is still active

    begin -- local exception handler
      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore the old table and its indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new table
	raise exception esql, eisam;
      end exception;

      rename table panel_count_new to panel_count;

      let errorHint = "populate table";	

      -- primary key

      create unique index panel_count_primary_key_index
  	on panel_count (panelcnt_panel_zdb_id, panelcnt_mrkr_type,
	panelcnt_or_lg)
  	in idxdbs2;

      alter table panel_count
  	add constraint
   	 primary key (panelcnt_panel_zdb_id, panelcnt_mrkr_type,
	panelcnt_or_lg)
    	constraint panel_count_primary_key;

      -- foreign keys

     create index panelcnt_panel_zdb_id
  	on panel_count(panelcnt_panel_zdb_id)
  	in idxdbs2;

     alter table panel_count
	  add constraint
    	foreign key (panelcnt_panel_zdb_id)
    	references panels
    	on delete cascade
    	constraint panelcnt_panel_zdb_id_foreign_key;


     create index panelcnt_mrkr_type_index
  	on panel_count(panelcnt_mrkr_type)
  	in idxdbs2;

    alter table panel_count
  	add constraint
    	foreign key (panelcnt_mrkr_type)
    	references marker_types
    	on delete cascade
    	constraint panelcnt_mrkr_type_foreign_key;

    create index panelcnt_or_lg_index
  	on panel_count(panelcnt_or_lg)
  	in idxdbs2;

    alter table panel_count
  	add constraint
   	 foreign key (panelcnt_or_lg)
    	references linkage_group
    	on delete cascade
    	constraint panelcnt_or_lg_foreign_key;

     grant select on panel_count to "public";
     grant update on panel_count to "public";
     grant insert on panel_count to "public";
     grant delete on panel_count to "public";
    
   end -- Local exception handler

    commit work;

  end -- Global exception handler

  -- Update statistics on table that was just created.

  begin work;

  update statistics high for table panel_count;

  commit work;

  return 0;

end function;

grant execute on function "informix".regen_panelcount() 
  to "public" as "informix";
