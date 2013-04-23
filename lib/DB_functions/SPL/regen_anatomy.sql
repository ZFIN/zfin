
-- Estimated Run Time: 3 hours
--test
-- Drop all out-of-date functions.
--drop function regen_anatomy;
drop function populate_anat_display_stage_children;


{  Comment this out.  Use when you are trying to speed this up.
-- Create function for logging timings.
drop procedure regen_anatomy_log;

create procedure regen_anatomy_log(log_message lvarchar)

  define echoCommand lvarchar;

  let echoCommand = 'echo "' || get_time() || ' ' || log_message ||
		       '" >> /tmp/regen_anatomy_log.<!--|DB_NAME|-->';
  system echoCommand;

end procedure;
}

-- Create recursive functions in reverse order of execution.

-- ---------------------------------------------------------------------
-- POPULATE_ANAT_DISPLAY_STAGE_CHILDREN
-- ---------------------------------------------------------------------

create function populate_anat_display_stage_children(stageId varchar(80), 
						     parentId varchar(50),
			 			     parentName varchar(50),
						     indent int, 
						     seqNum int)
  returning int;

  -- It is initiated by regen_anatomy, and it calls itself recursively
  -- to populate anatomy_display_new table by using the intermediate table
  -- stage_item_contained and stage_item_child_list.

  define childIndent int;
  define childId like term.term_zdb_id;
  define childName like term.term_name;
  define childNameOrder like term.term_name;

  -- insert record into anatomy_display from passed in values
  insert into anatomy_display_new 
    	values(stageId,seqNum,parentId,parentName,indent);

  if (indent = 1) then
    -- this is a root item for the stage, it needs to be deleted from sic.
    delete from stage_items_contained where sic_anatitem_zdb_id = parentId;
  end if -- else it was called by this function -implying it was deleted.

  -- increment instance variables
  let seqNum = seqNum + 1;
  let childIndent = indent + 1;

  -- Save the direct descendant in stage_item_child_list table, and 
  -- delete it from stage_item_contained table. Thus, each anatomy item
  -- would only have one display with it highest level at a particular stage.
  foreach
     select sic_anatitem_zdb_id, term_name, term_name
       into childId, childName, childNameOrder
       from term_relationship, stage_items_contained, term
      where parentId = termrel_term_1_zdb_id
        and sic_anatitem_zdb_id = termrel_term_2_zdb_id
	and sic_anatitem_zdb_id = term_zdb_id

    insert into stage_item_child_list 
         values(parentId,childId,childName, childNameOrder);

    delete from stage_items_contained where sic_anatitem_zdb_id = childId;

   end foreach 
  
   -- For each direct descendant saved in stage_item_child_list, recursively
   -- call the function to populate further descendant.
   foreach 
     select stimchilis_child_zdb_id, stimchilis_child_name, stimchilis_child_name_order
       into childId, childName, childNameOrder
       from stage_item_child_list
      where stimchilis_item_zdb_id = parentId
      order by stimchilis_child_name_order

      execute function populate_anat_display_stage_children( 
        		stageId, childId, childName, childIndent,seqNum) 
      into seqNum;	
 
    end foreach

  return seqNum;
end function;

update statistics for function populate_anat_display_stage_children;

-- ---------------------------------------------------------------------
-- REGEN_ANATOMY
-- ---------------------------------------------------------------------

create dba function "informix".regen_anatomy()
  returning integer

  -- populates anatomy fast search tables:
  --  anatomy_display: term's position in a dag display of a certain stage
  --  all_term_contains(?): each and every ancestor and descendant


  -- see regen_names.sql for details on how to debug SPL routines.

  set debug file to "/tmp/debug_regen_anatomy.ogodb";
-- trace on;

  begin	-- global exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define zdbFlagReturn integer;

    define stageId 	like stage.stg_zdb_id;
    define seqNum int;
    define indent int;
    define anatomyId like term.term_zdb_id;
    define anatomyName like term.term_name;
    define stgHoursStart like stage.stg_hours_start;

    define nRows int;	
    
    define nSynonyms int;
    define nGenesForThisItem int;
    define nGenesForChildItems int;
    define nDistinctGenes int;
    define nGenosForThisItem int;
    define nGenosForChildItems int;
    define nDistinctGenos int;

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get out, and leave the original tables around

	on exception in (-255, -668)
          --  255: OK to get a "Not in transaction" here, since
          --       we might not be in a transaction when the rollback work 
          --       below is performed.
          --  668: OK to get a "System command not executed" here.
          --       Is probably the result of the chmod failing because we
          --       are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
                               ' SQL Error: ' || sqlError::varchar(200) || 
                               ' ISAM Error: ' || isamError::varchar(200) ||
                               ' ErrorText: ' || errorText ||
                               ' ErrorHint: ' || errorHint ||
                               '" >> /tmp/regen_anatomy_exception.<!--|DB_NAME|-->';
        system exceptionMessage;

        -- Change the mode of the regen_anatomy_exception file.  This is
        -- only needed the first time it is created.  This allows us to 
        -- rerun the function from dbaccess as whatever user we want, and
	-- to reuse an existing regen_anatomy_exception file.

        system '/bin/chmod 666 /tmp/regen_anatomy_exception.<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

        -- Don't drop the tables here.  Leave them around in an effort to
        -- figure out what went wrong.

        let zdbFlagReturn = release_zdb_flag("regen_anatomy");
	return -1;
      end
    end exception;

      -- set standard set of session params
      let errorHint = "Setting session parameters";
      execute procedure set_session_params();
      

      --   GRAB ZDB_FLAG

      let errorHint = "Grab zdb_flag";
      if grab_zdb_flag("regen_anatomy") <> 0 then
        return 1;
      end if


      -- =========  CREATE TABLES THAT ONLY EXIST IN THIS FUNCTION  =========
	
      let errorhint = "create all_anatomy_stage";

      -- ---- ALL_ANATOMY_STAGE ----

      -- a table contains every anatomy term and every stage the term is in. 
      -- the "Unknown" stage is excluded. 
      --
      -- This table is used to populate stage_items_contained. 
  
      if (exists (select *
	           from systables
		   where tabname = "all_anatomy_stage")) then
	drop table all_anatomy_stage;
      end if

      create table all_anatomy_stage 
	(
	  allanatstg_stg_zdb_id	      varchar(50),
	  allanatstg_anat_item_zdb_id varchar(50)
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 128 next size 128 
	lock mode page;

      -- create temporary indexes, these are dropped when table is renamed.

      create unique index all_anatomy_stage_primary_key_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id,
				  allanatstg_stg_zdb_id)
        in idxdbs1;

      create index allanatstg_stg_zdb_id_index
        on all_anatomy_stage (allanatstg_stg_zdb_id)
	in idxdbs1;


      -- ---- STAGE_ITEMS_CONTAINED ----

      let errorhint = "Creating stage_items_contained";

      -- this table is populated/repopulated with all anatomy items existing
      -- at ONE stage a time. 
      -- It is used in the process of populating anatomy_display table.

      if (exists (select *
	           from systables
		   where tabname = "stage_items_contained")) then
	drop table stage_items_contained;
      end if

      create table stage_items_contained
	(
	  sic_anatitem_zdb_id 	varchar(50)
	)
	in tbldbs3
	extent size 64 next size 64
	lock mode page;

      -- primary key
 
      create unique index stage_items_contained_primary_key_index 
	on stage_items_contained (sic_anatitem_zdb_id) 
	in idxdbs2;
      alter table stage_items_contained add constraint
	primary key (sic_anatitem_zdb_id)
	constraint stage_items_contained_primary_key;

      -- foreign keys

      alter table stage_items_contained add constraint
	foreign key (sic_anatitem_zdb_id)
	references term
	constraint sic_anatitem_zdb_id_foreign_key;
  

      -- ---- STAGE_ITEM_CHILD_LIST ----

      let errorhint = "Creating stage_item_child_list";

      -- this table is populated/repopulated with anatomy relationship pairs
      -- at ONE stage a time.
      -- It is used in the process of populating anatomy_display table.
  
      if (exists (select *
	           from systables
		   where tabname = "stage_item_child_list")) then
	drop table stage_item_child_list;
      end if

      create table stage_item_child_list
	(
	  stimchilis_item_zdb_id	varchar(50),
	  stimchilis_child_zdb_id	varchar(50),
	  stimchilis_child_name		varchar(80)
	    not null
	      constraint stimchilis_child_name_not_null,
	  stimchilis_child_name_order	varchar(100)
	    not null
	      constraint stimchilis_child_name_order_not_null
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 512 next size 512
	lock mode page;

      -- primary key

      create unique index stage_item_child_list_primary_key_index
        on stage_item_child_list (stimchilis_item_zdb_id,
				  stimchilis_child_zdb_id)  
	in idxdbs1;
      alter table stage_item_child_list add constraint
        primary key (stimchilis_item_zdb_id,
		     stimchilis_child_zdb_id)
        constraint stage_item_child_list_primary_key;

      -- foreign keys
      create index stimchilis_item_zdb_id_index
        on stage_item_child_list (stimchilis_item_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_item_zdb_id)
	references term
	constraint stimchilis_item_zdb_id_foreign_key;

      create index stimchilis_child_zdb_id_index
        on stage_item_child_list (stimchilis_child_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_child_zdb_id)
	references term
	constraint stimchilis_child_zdb_id_foreign_key;




      -- =================    CREATE OUTPUT TABLES     =======================

      let errorhint = "create fast search tables";

      --use a new table to collect data in case of an error; drop the old table
      --and rename the new table before returning.

      -- ---- ANATOMY_DISPLAY ----

      -- This table stores every stage except the Unknown stage, and every 
      -- anatomy item that exists at this stage along with its row(seq_num)
      -- and column(indent) coordinates in a tree format display.  

      let errorHint = "Creating anatomy_display_new";
      if (exists (select *
	           from systables
		   where tabname = "anatomy_display_new")) then
	drop table anatomy_display_new;
      end if

      create table anatomy_display_new
	(
	  anatdisp_stg_zdb_id		varchar(50),
	  anatdisp_seq_num		integer,
	  anatdisp_item_zdb_id		varchar(50) not null,
	  anatdisp_item_name		varchar(80)  not null,
	  anatdisp_indent		integer not null
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 256 next size 256 
	lock mode page;

      -- =================   POPULATE TABLES   ===============================

      -- ---------------------------------------------------
      --    ALL_ANATOMY_STAGE
      -- ---------------------------------------------------
      let errorhint = "Populating all_anatomy_stage";

      -- For each term_zdb_id, find all stages this item occurs in.

      -- if the start stage of an anatomy item is Unknown, only 
      -- insert then end stage, and vice verse.
      insert into all_anatomy_stage
    	select ts_end_stg_zdb_id, ts_term_zdb_id
	  from term_stage, stage
	 where ts_start_stg_zdb_id = stg_zdb_id
           and stg_name = "Unknown";
  
      insert into all_anatomy_stage
    	select ts_start_stg_zdb_id, ts_term_zdb_id
	  from term_stage, stage
	 where ts_end_stg_zdb_id = stg_zdb_id
           and stg_name = "Unknown";

      -- for all other anatitems find stages it is contained in
      insert into all_anatomy_stage
    	select s1.stg_zdb_id, term_zdb_id
	  from stage s1, term_stage, term
	  where term_name <> 'not specified'
	    and s1.stg_name <> 'Unknown'
	    and term_zdb_id = ts_term_zdb_id
	    and exists 
		(  select *
		     from stage ss, stage se
		    where ts_start_stg_zdb_id = ss.stg_zdb_id
		      and ss.stg_name <> "Unknown"
		      and ts_end_stg_zdb_id = se.stg_zdb_id
		      and se.stg_name <> "Unknown"       
		      and s1.stg_hours_start >= ss.stg_hours_start
		      and s1.stg_hours_end <= se.stg_hours_end        
		);
		
      update statistics high for table all_anatomy_stage;	
      
      -- -----------------------------------------------------------------------
      --    ANATOMY_DISPLAY_NEW
      -- -----------------------------------------------------------------------
	let errorhint = "Populating anatomy_display_new";

      -- Anatomy_display has variables that are stage based, so use each
      -- stage_id to insert associated term.

      foreach
	select stg_zdb_id 
	  into stageId 
	  from stage
         
        -- Start the seqNum at zero and indent at one.
        let seqNum = 0; 
	let indent = 1;

        -- populate stage_items_contained with anatomy term in this stage
	insert into stage_items_contained 
		select allanatstg_anat_item_zdb_id
		  from all_anatomy_stage
		 where allanatstg_stg_zdb_id = stageId;

        -- find out the highest level term in this stage, call
        -- populate_anat_display_stage_children to recursively retrieve children
trace on;
	foreach 
      	     select crt.sic_anatitem_zdb_id, term_name, stg_hours_start 
               into anatomyId, anatomyName, stgHoursStart
               from stage_items_contained crt, term_stage, term , stage
      	      where crt.sic_anatitem_zdb_id = term_zdb_id
	        and ts_start_stg_zdb_id = stg_zdb_id
		and term_zdb_Id = ts_term_zdb_id
 	        and not exists
		  ( select * 
	    	      from term_relationship, stage_items_contained prt
	             where prt.sic_anatitem_zdb_id = termrel_term_1_zdb_id
	     	       and crt.sic_anatitem_zdb_id = termrel_term_2_zdb_id
		   )
            order by stg_hours_start, term_name

      	    execute function populate_anat_display_stage_children(
        		stageId, anatomyId, anatomyName, indent, seqNum) 
            into seqNum;

        end foreach

        -- clean up the stage specific tables for the next iteration
        delete from stage_items_contained;
        delete from stage_item_child_list;

      end foreach



    -- -------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- -------------------------------------------------------------------------

    let errorHint = "Renaming tables";

    begin work;


    -- Delete the old tables.  Some may not exist (if the DB has just
    -- been created), so ignore errors from the drops.

    begin -- local exception handler for dropping of original tables

      on exception in (-206)
	      -- ignore any table that doesn't already exist
      end exception with resume;

      drop table stage_items_contained;
      drop table stage_item_child_list;
      drop table all_anatomy_stage;
      drop table anatomy_display;

    end -- local exception handler for dropping of original tables

    -- Now rename our new tables to have the permanent names.

    -- This also requires dropping and recreating any indexes that were created
    -- with the temporary names.  Informix does not support renaming existing 
    -- indexes.  We can't just use the permanent names to begin with because
    -- they conflict with the names of the indexes on the prior version of the
    -- tables.

    -- Note that the exception-handler at the top of this file is still active

    begin -- local exception handler
      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

 
      -- ---- ANATOMY_DISPLAY ----

      rename table anatomy_display_new to anatomy_display;

      -- primary key

      create unique index anatomy_display_primary_key_index
        on anatomy_display (anatdisp_stg_zdb_id,
			    anatdisp_seq_num)
	
	in idxdbs1;
      alter table anatomy_display add constraint 
        primary key (anatdisp_stg_zdb_id, anatdisp_seq_num)
	constraint anatomy_display_primary_key;

      -- foreign keys

      let errorHint = "anatdisp_stg_zdb_id_index";
      create index anatdisp_stg_zdb_id_index
        on anatomy_display (anatdisp_stg_zdb_id)
	
	in idxdbs1;
      alter table anatomy_display add constraint
        foreign key (anatdisp_stg_zdb_id)
	references stage
	  on delete cascade
	constraint anatdisp_stg_zdb_id_foreign_key;

      let errorHint = "anatdisp_item_zdb_id_index";
      create index anatdisp_item_zdb_id_index
        on anatomy_display (anatdisp_item_zdb_id)
	
	in idxdbs1;
      alter table anatomy_display add constraint
        foreign key (anatdisp_item_zdb_id)
	references term
	  on delete cascade
	constraint anatdisp_item_zdb_id_foreign_key;

      let errorHint = "anatdisp_item_name_index";
      create index anatdisp_item_name_index
        on anatomy_display (anatdisp_item_name)
	
	in idxdbs1;


    end -- Local exception handler
    commit work;
    
  end -- Global exception handler
  
  -- Update statistics on tables that were just created.

  begin work;
  update statistics high for table anatomy_display;
  commit work;

  --   RELEASE ZDB_FLAG

  if release_zdb_flag("regen_anatomy") <> 0 then
    return 1;
  end if

  return 0;

end function;

grant execute on function "informix".regen_anatomy () 
  to "public" as "informix";
  
