
-- Estimated Run Time: 3 hours

-- Drop all out-of-date functions.
drop function regen_anatomy;
drop function populate_all_anatomy_contains;




-- ---------------------------------------------------------------------
-- POPULATE_ALL_ANATOMY_CONTAINS
-- ---------------------------------------------------------------------

create function populate_all_anatomy_contains()
  returning integer

  -- find the transitive closure of anatomy contains, 
  -- keeping only the closest ancestor

  -- called from regen_anatomy

  define dist int;
  define delta int;

  let dist = 1;
  let delta = -1;

  -- the first level is a gimmie from anatomy_relationship
  -- also _all_ child nodes are explicitly listed
  -- so we only need to find ancestors of these child nodes
  insert into all_anatomy_contains_new (allanatcon_container_zdb_id, 
  					allanatcon_contained_zdb_id,
  					allanatcon_min_contain_distance)
    select anatrel_anatitem_1_zdb_id,
	   anatrel_anatitem_2_zdb_id, 
	   dist
    from anatomy_relationship
    where anatrel_type in ('is_a','part_of');

  -- continue as long as progress is made 
  -- there may be more elegant ways to do this so please do tell. 
  while (delta  <  (select count(*) from all_anatomy_contains_new) )
    let dist = dist + 1;
    -- set the baseline for determining is progress is made
    select count(*) 
      into delta 
      from all_anatomy_contains_new; 
		
    -- try adding new ancestors 
    insert into all_anatomy_contains_new
      select distinct a.anatrel_anatitem_1_zdb_id,     -- A.ancestor
		      b.allanatcon_containeD_zdb_id,  -- B.child
		      dist                             -- min depth
	from anatomy_relationship a,            -- source of all ancestors
             all_anatomy_contains_new b     -- source of all childs 

	where b.allanatcon_min_contain_distance = (dist - 1) 
	      -- limit the search to the previous level          
          and b.allanatcon_containeR_zdb_id = a.anatrel_anatitem_2_zdb_id
	      -- B.ancestor == A.child
	      -- checking for duplicates here is where the time gets absurd  
	      -- (2:30 vs 0:06), so 
	      --   "kill em all and let god sort them out later"
	  and a.anatrel_type in ('is_a', 'part_of')
	      -- all_anatomy_contains doesn't want develops_from relationships,
	      -- and it's better to explicitly include rather than exclude,
	      -- since we want the behavior to stay the same the next time
	      -- a new type is added
	  ;

  end while

    
  -- split out the keepers in one step usings the dbs strength with set 
  -- operations instead of n-1 peicemeal steps 
  select allanatcon_container_zdb_id,
	 allanatcon_contained_zdb_id,
	 min(allanatcon_min_contain_distance) as allanatcon_min_contain_distance
    from all_anatomy_contains_new
    group by allanatcon_container_zdb_id, allanatcon_contained_zdb_id
    into temp all_anatomy_contains_new_tmp with no log
    ;

  -- move the keepers to where they will live
  delete from all_anatomy_contains_new;
  insert into all_anatomy_contains_new 
    select distinct * 
      from all_anatomy_contains_new_tmp
      ;

  -- return the number of rows kept as an hint of correctness
  select count(*) 
    into delta 
    from all_anatomy_contains_new; 

  return delta;				  

end function;

update statistics for function populate_all_anatomy_contains;


-- ---------------------------------------------------------------------
-- REGEN_ANATOMY
-- ---------------------------------------------------------------------

create dba function "informix".regen_anatomy()
  returning integer

  -- populates anatomy fast search tables:
  --  anatomy_display: term's position in a dag display of a certain stage
  --  all_anatomy_contains: each and every ancestor and descendant


  -- see regen_names.sql for details on how to debug SPL routines.

  set debug file to "/tmp/debug_regen_anatomy.<!--|DB_NAME|-->";
  --trace on;

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
    define anatomyId like anatomy_item.anatitem_zdb_id;
    define anatomyName like anatomy_item.anatitem_name;
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




      -- =================    CREATE OUTPUT TABLES     =======================

      let errorhint = "create fast search tables";

      --use a new table to collect data in case of an error; drop the old table
      --and rename the new table before returning.

      -- ---- ANATOMY_DISPLAY ----


      -- ---- ALL_ANATOMY_CONTAINS ----
      let errorHint = "Creating all_anatomy_contains_new";
      -- this table stores every anatomy term with every ancestor 
      -- that has a contains relationship and the shortest distance 
      -- between each pair.  In this case, a contains relationship
      -- is being defined as type's "is_a" and "part_of", which
      -- leaves out "develops_from".  Unfortunately, the only place
      -- where that is defined is in this file, when we have a generic
      -- DAG, we will probably need relationship type groups so that
      -- nothing has to be hardcoded.

      let errorHint = "Creating all_anatomy_contains_new";
      if (exists (select *
		   from systables
		   where tabname = "all_anatomy_contains_new")) then
        drop table all_anatomy_contains_new;
      end if

      create table all_anatomy_contains_new
        (
	  allanatcon_container_zdb_id		varchar(50),
	  allanatcon_contained_zdb_id		varchar(50),
	  allanatcon_min_contain_distance	integer not null
        )
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 256 next size 256 
	lock mode page;

      -- create temp index.  dropped when table renamed
      create index all_anatomy_contains_new_primary_key_index
        on all_anatomy_contains_new (allanatcon_container_zdb_id,     
				     allanatcon_contained_zdb_id)
	in idxdbs2;


      -- =================   POPULATE TABLES   ===============================


      -- -----------------------------------------------------------------------
      --     ALL_ANATOMY_CONTAINS_NEW
      -- -----------------------------------------------------------------------

      let errorHint = "Populating all_anatomy_contains_new";

      execute function populate_all_anatomy_contains()
        into nRows;

      update statistics high for table all_anatomy_contains_new;

      
 
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


      drop table all_anatomy_contains;

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

 
 
     
      -- ---- ALL_ANATOMY_CONTAINS ----

      rename table all_anatomy_contains_new to all_anatomy_contains;

      -- primary key

      let errorHint = "all_anatomy_contains_primary_key_index";
      drop index all_anatomy_contains_new_primary_key_index;
      create unique index all_anatomy_contains_primary_key_index
        on all_anatomy_contains (allanatcon_container_zdb_id,     
				 allanatcon_contained_zdb_id)
	
	in idxdbs2;
      alter table all_anatomy_contains add constraint
        primary key (allanatcon_container_zdb_id,     
		     allanatcon_contained_zdb_id)
	constraint all_anatomy_contains_primary_key;

      -- foreign keys

      let errorHint = "allanatcon_container_zdb_id_index";
      create index allanatcon_container_zdb_id_index
        on all_anatomy_contains (allanatcon_container_zdb_id)
	
	in idxdbs2;
      alter table all_anatomy_contains add constraint
        foreign key (allanatcon_container_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint allantcon_container_zdb_id_foreign_key;

      let errorHint = "allanatcon_contained_zdb_id_index";
      create index allanatcon_contained_zdb_id_index
        on all_anatomy_contains (allanatcon_contained_zdb_id)
	
	in idxdbs2;
      alter table all_anatomy_contains add constraint
        foreign key (allanatcon_contained_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint allantcon_contained_zdb_id_foreign_key;

            
    end -- Local exception handler
    commit work;
    
  end -- Global exception handler
  
  -- Update statistics on tables that were just created.

  begin work;

  update statistics high for table all_anatomy_contains;
  commit work;

  --   RELEASE ZDB_FLAG

  if release_zdb_flag("regen_anatomy") <> 0 then
    return 1;
  end if

  -- re-create self-records in all_anatomy_contains
  insert into all_anatomy_contains
              select anatitem_zdb_id,anatitem_zdb_id,0
              from anatomy_item;

  return 0;

end function;

grant execute on function "informix".regen_anatomy () 
  to "public" as "informix";
  
