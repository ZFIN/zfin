

-- Drop all out-of-date functions.
drop function regen_anatomy;
drop function populate_all_anatomy_contains;
drop procedure populate_anat_display_stage;
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

create function populate_anat_display_stage_children(parent_id varchar(50), 
						     indent int, 
						     seq_num int, 
						     hier_code varchar(50), 
						     stage_id varchar(80), 
						     anatomy_name varchar(50))
  returning int;

  -- Receives parent anatitem_zdb, stage_id, anatomy_name and 
  -- initial values for instance variables.
  -- Called by populate_anat_display_stage procedure.

  define child_indent int;
  define child_id like anatomy_item.anatitem_zdb_id;
  define anatomy_order like anatomy_item.anatitem_name_order;
  define distance int;

  -- insert record into anatomy_display from passed in values
  insert into anatomy_display_new 
    values(hier_code,stage_id,seq_num,parent_id,anatomy_name,indent);

  if (indent = 1) then
    -- this is a root item for the stage, it needs to be deleted from sic.
    delete from stage_items_contained where sic_anatitem_zdb_id = parent_id;
  end if -- else it was called by this function -implying it was deleted.

  -- increment instance variables
  let seq_num = seq_num+1;
  let child_indent = indent+1;

  -- call function recursively for each anatitem that is contained by 
  -- the parent anatitem and 
  foreach
     select anatrel_anatitem_2_zdb_id, anatitem_type_code, anatitem_name, anatitem_name_order
       into child_id, hier_code, anatomy_name, anatomy_order
       from anatomy_item, anatomy_relationship, stage_items_contained
      where parent_id = anatrel_anatitem_1_zdb_id
        and anatitem_zdb_id = anatrel_anatitem_2_zdb_id
        and anatitem_zdb_id = sic_anatitem_zdb_id
        and sic_stg_zdb_id = stage_id
   order by anatitem_name_order

    insert into stage_item_child_list 
      values(stage_id,parent_id,child_id,anatomy_name,hier_code,anatomy_order);

    delete from stage_items_contained where sic_anatitem_zdb_id = child_id;

  end foreach -- retrieval of children

  foreach
    select stimchilis_child_zdb_id, stimchilis_anat_name, stimchilis_anat_order, stimchilis_hier_code
      into child_id, anatomy_name, anatomy_order, hier_code
      from stage_item_child_list
     where stimchilis_stg_zdb_id = stage_id
       and stimchilis_item_zdb_id = parent_id
  order by stimchilis_anat_order --new

    execute function populate_anat_display_stage_children( 
        child_id,child_indent,seq_num,hier_code,stage_id,anatomy_name ) 
      into seq_num;	

  end foreach

  return seq_num;
end function;

update statistics for function populate_anat_display_stage_children;


-- ---------------------------------------------------------------------
-- POPULATE_ANAT_DISPLAY_STAGE
-- ---------------------------------------------------------------------

create procedure populate_anat_display_stage(stage_id varchar(50))

  -- For each stage, find the root anatitem(s) ie. the anatitems that don't
  -- have a parent in the same stage.  Execute populate...stage_children for
  -- each root anatitem.
  -- Called from regen_anatomy.

  define seqNum int;
  define indent int;
  define distance int;
  define hierCode, prevCode like anatomy_hierarchy.anathier_code;
  define anatomyId like anatomy_item.anatitem_zdb_id;
  define anatomy_name, lowercase_anatitem_name like anatomy_item.anatitem_name;
  define temp int;

  -- Start the seq_num at zero and indent at one.
  let seqNum = 0;
  let indent = 1;
  let prevCode = '  ';
  let distance = 0;
  let temp = 0;


  --get the relevant children for the stage and insert the stage/item pairs
  --into the temp stage_items_contained table.
  foreach
    select distinct allanatstg_anat_item_zdb_id
      into anatomyId
      from all_anatomy_stage_new
      where allanatstg_stg_zdb_id = stage_id
 
    insert into stage_items_contained
      values(stage_id,anatomyId);

  end foreach


  foreach
    select a1.anatitem_zdb_id, anathier_code, a1.anatitem_name, a1.anatitem_name_lower, s.stg_hours_start --new
      into anatomyId, hierCode, anatomy_name, lowercase_anatitem_name, temp
      from stage s, stage_items_contained, anatomy_hierarchy, anatomy_item a1
      where sic_anatitem_zdb_id = a1.anatitem_zdb_id
        and anathier_code = a1.anatitem_type_code
	and a1.anatitem_start_stg_zdb_id = s.stg_zdb_id
	and not exists
	(
	  select * 
	  from stage_items_contained, anatomy_relationship, anatomy_item a2
	  where a1.anatitem_zdb_id = anatrel_anatitem_2_zdb_id
	    and a2.anatitem_zdb_id = anatrel_anatitem_1_zdb_id 
	    and sic_anatitem_zdb_id = anatrel_anatitem_1_zdb_id
	)
	--and anathier_code = 'ST' --new
      order by s.stg_hours_start --new anathier_code, anatitem_name

    if hierCode = prevCode then
      execute function populate_anat_display_stage_children(
        anatomyId, indent, seqNum, hierCode, stage_id, anatomy_name ) 
        into seqNum;
    else 
      execute function populate_anat_display_stage_children(
        anatomyId, 1, seqNum, hierCode, stage_id, anatomy_name ) 
      into seqNum;
    end if

    delete from stage_item_child_list 
      where stimchilis_stg_zdb_id = stage_id 
        and stimchilis_item_zdb_id = anatomyId; 
    let prevCode = hierCode;

  end foreach

end procedure;

update statistics for procedure populate_anat_display_stage;


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
  insert into all_anatomy_contains_new
    select anatrel_anatitem_1_zdb_id ,
	   anatrel_anatitem_2_zdb_id, 
	   dist
      from anatomy_relationship;

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
		      dist                            -- min depth
	from anatomy_relationship a,            -- source of all ancestors
             all_anatomy_contains_new b     -- source of all childs 

	where b.allanatcon_min_contain_distance = (dist - 1) 
	      -- limit the search to the previous level          
          and b.allanatcon_containeR_zdb_id = a.anatrel_anatitem_2_zdb_id
	      -- B.ancestor == A.child
	      -- checking for duplicates here is where the time gets absurd  
	      -- (2:30 vs 0:06), so 
	      --   "kill em all and let god sort them out later"
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

  -- populates anatomy fast search tables.

  -- see regen_genomics.sql for details on how to debug SPL routines.

  --set debug file to "/tmp/debug_regen_anatomy.<!--|DB_NAME|-->";
  --trace on;

  begin	-- global exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);

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

	update zdb_flag set zflag_is_on = 'f'
		where zflag_name = "regen_anatomy" 
	 	  and zflag_is_on = 't'; 
	return -1;
      end
    end exception;

    --trace on;

    begin

      define nrows integer;

      let errorHint = "zdb_flag";

      -- zdb_flag

      update zdb_flag set zflag_is_on = 't'
	  where zflag_name = "regen_anatomy" 
	    and zflag_is_on = 'f';

      let nrows = DBINFO('sqlca.sqlerrd2');

      if (nrows == 0)	then
	  return 1;
      end if

      update zdb_flag set zflag_last_modified = CURRENT
	  where zflag_name = "regen_anatomy";


      -- crank up the parallelism.

      set pdqpriority high;

      -- ======  CREATE TABLES THAT ONLY EXIST IN THIS FUNCTION  ======

      -- ---- STAGE_ITEMS_CONTAINED ----

      if (exists (select *
	           from systables
		   where tabname = "stage_items_contained")) then
	drop table stage_items_contained;
      end if

      create table stage_items_contained
	(
	  sic_stg_zdb_id 	varchar(50),
	  sic_anatitem_zdb_id 	varchar(50)
	)
	in tbldbs3
	extent size 64 next size 64
	lock mode page;

      -- primary key

      create unique index stage_items_contained_primary_key_index 
	on stage_items_contained (sic_stg_zdb_id,sic_anatitem_zdb_id) 
	in idxdbs2;
      alter table stage_items_contained add constraint
	primary key (sic_stg_zdb_id,sic_anatitem_zdb_id)
	constraint stage_items_contained_primary_key;

      -- foreign keys

      create index sic_stg_zdb_id_index
	on stage_items_contained (sic_stg_zdb_id)
	in idxdbs2;
      alter table stage_items_contained add constraint
	foreign key (sic_stg_zdb_id)
	references stage
	constraint sic_stg_zdb_id_foreign_key;

      create index sic_anatitem_zdb_id_index
	on stage_items_contained (sic_anatitem_zdb_id)
	in idxdbs2;
      alter table stage_items_contained add constraint
	foreign key (sic_anatitem_zdb_id)
	references anatomy_item
	constraint sic_anatitem_zdb_id_foreign_key;
  


      -- ---- STAGE_ITEM_CHILD_LIST ----

      if (exists (select *
	           from systables
		   where tabname = "stage_item_child_list")) then
	drop table stage_item_child_list;
      end if

      create table stage_item_child_list
	(
	  stimchilis_stg_zdb_id		varchar(50),
	  stimchilis_item_zdb_id	varchar(50),
	  stimchilis_child_zdb_id	varchar(50),
	  stimchilis_anat_name		varchar(80)
	    not null
	      constraint stimchilis_anat_name_not_null,
	  stimchilis_hier_code		char(5)
	    not null
	      constraint stimchilis_hier_code_not_null,
	  stimchilis_anat_order		varchar(100)
	    not null
	      constraint stimchilis_anat_order_not_null
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 512 next size 512
	lock mode page;

      -- primary key

      create unique index stage_item_child_list_primary_key_index
        on stage_item_child_list (stimchilis_stg_zdb_id,
				  stimchilis_item_zdb_id,
				  stimchilis_child_zdb_id)  
	in idxdbs1;
      alter table stage_item_child_list add constraint
        primary key (stimchilis_stg_zdb_id,
		     stimchilis_item_zdb_id,
		     stimchilis_child_zdb_id)
        constraint stage_item_child_list_primary_key;

      -- foreign keys

      create index stimchilis_stg_zdb_id_index
        on stage_item_child_list (stimchilis_stg_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_stg_zdb_id)
	references stage
	constraint stimchilis_stg_zdb_id_foreign_key;

      create index stimchilis_item_zdb_id_index
        on stage_item_child_list (stimchilis_item_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_item_zdb_id)
	references anatomy_item
	constraint stimchilis_item_zdb_id_foreign_key;

      create index stimchilis_child_zdb_id_index
        on stage_item_child_list (stimchilis_child_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_child_zdb_id)
	references anatomy_item
	constraint stimchilis_child_zdb_id_foreign_key;



      -- ======  CREATE OUTPUT TABLES  ======

      -- ---- ALL_ANATOMY_STAGE ----

      --use a new table to collect data in case of an error; drop the old table
      --and rename the new table before returning.

      if (exists (select *
	           from systables
		   where tabname = "all_anatomy_stage_new")) then
	drop table all_anatomy_stage_new;
      end if

      create table all_anatomy_stage_new 
	(
	  allanatstg_anat_item_zdb_id varchar(50),
	  allanatstg_stg_zdb_id	      varchar(50)
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 128 next size 128 
	lock mode page;

      -- create temporary indexes, these are dropped when table is renamed.

      create unique index all_anatomy_stage_new_primary_key_index
        on all_anatomy_stage_new (allanatstg_anat_item_zdb_id,
				  allanatstg_stg_zdb_id)
        in idxdbs1;

      create index allanatstg_new_stg_zdb_id_index
        on all_anatomy_stage_new (allanatstg_stg_zdb_id)
	in idxdbs1;


      -- ---- ANATOMY_DISPLAY ----

      if (exists (select *
	           from systables
		   where tabname = "anatomy_display_new")) then
	drop table anatomy_display_new;
      end if

      create table anatomy_display_new
	(
	  anatdisp_hier_code		char(2),
	  anatdisp_stg_zdb_id		varchar(50),
	  anatdisp_seq_num		integer,
	  anatdisp_item_zdb_id		varchar(50) 
	    not null,
	  anatdisp_item_name		varchar(80) 
	    not null,
	  anatdisp_indent		integer
	    not null
	)
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 256 next size 256 
	lock mode page;



      -- ---- ANATOMY_STATS ----

      if (exists (select *
	           from systables
		   where tabname = "anatomy_stats_new")) then
	drop table anatomy_stats_new;
      end if

      create table anatomy_stats_new 
	(
	  anatstat_anatitem_zdb_id          varchar(50),
	  anatstat_object_type              char(32),
	  anatstat_synonym_count	    integer
	     not null,
	  anatstat_anatitem_count           integer
	     not null,
	  anatstat_contains_count           integer
	     not null,
	  anatstat_total_distinct_count     integer
	     not null
        )
	in tbldbs2
	extent size 128 next size 128 
	lock mode page;


      -- ---- ANATOMY_STAGE_STATS ----

      if (exists (select *
	           from systables
		   where tabname = "anatomy_stage_stats_new")) then
	drop table anatomy_stage_stats_new;
      end if

      create table anatomy_stage_stats_new 
	(
	  anatstgstat_anatitem_zdb_id          varchar(50),
	  anatstgstat_stg_zdb_id                varchar(50),
	  anatstgstat_object_type               char(32),
	  anatstgstat_anatitem_stg_count       integer
	     not null,
	  anatstgstat_contains_count            integer
	     not null,
	  anatstgstat_total_distinct_count               integer
	     not null
        )
	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
	extent size 256 next size 256 
	lock mode page;


      -- ---- ALL_ANATOMY_CONTAINS ----

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
    end


{
    create index non_parent_stage_start_hour_index 
      on non_parent_stage(start_hour)
	in idxdbs3;
    create index non_parent_stage_end_hour_index
      on non_parent_stage(end_hour)
	in idxdbs3;
	
    update statistics high for table non_parent_stage; 	
}

    begin
      -- ----------------------------------------------------------------------------
      -- begin populating ALL_ANATOMY_STAGE_NEW
      -- ----------------------------------------------------------------------------

      -- For each anatomy_item_zdb_id, find all stages the item occurs in,
      -- then insert the item and stage zdb_id's into all_anatomy stage.

      define stage_id like stage.stg_zdb_id;
      define item_id like anatomy_item.anatitem_zdb_id;
      define item_start_stg like stage.stg_zdb_id;
      define item_end_stg like stage.stg_zdb_id;

      --for each anatitem find all stages it is contained in
      insert into all_anatomy_stage_new
    	select anatitem_zdb_id, s1.stg_zdb_id
	  from stage s1, anatomy_item a1
	  where anatitem_name_lower not in ('structures', 'not specified')
	    and exists 
		(
		  select *
		    from stage s2, stage s3
		    where a1.anatitem_start_stg_zdb_id = s2.stg_zdb_id
		      and a1.anatitem_end_stg_zdb_id = s3.stg_zdb_id             
		      and (
			      (    s1.stg_hours_start >= s2.stg_hours_start
			       and s1.stg_hours_start < s3.stg_hours_end)
			   or
			      (    s1.stg_hours_end > s2.stg_hours_start
			       and s1.stg_hours_end <= s3.stg_hours_end)
			   or      
			      (    s1.stg_hours_start <= s2.stg_hours_start
			       and s1.stg_hours_end >= s3.stg_hours_end)
			 )           
		);
		
      update statistics high for table all_anatomy_stage_new;	
      
      -- done populating ALL_ANATOMY_STAGE_NEW

    end
    
    begin

      -- ----------------------------------------------------------------------------
      -- begin populating ANATOMY_DISPLAY_NEW
      -- ----------------------------------------------------------------------------

      -- Anatomy_display has variables that are stage based, so use each
      -- stage_id to insert associated anatomy_items. 

      define stage_ID like stage.stg_zdb_id;

      foreach
	select s1.stg_zdb_id 
	  into stage_ID 
	  from stage s1

	execute procedure populate_anat_display_stage(stage_ID);

      end foreach
      
      -- done populating ANATOMY_DISPLAY_NEW
    end

    begin
      -- ----------------------------------------------------------------------------
      -- begin populating ALL_ANATOMY_CONTAINS_NEW
      -- ----------------------------------------------------------------------------

      define nRows int;
      execute function populate_all_anatomy_contains()
        into nRows;

      update statistics high for table all_anatomy_contains_new;

      -- done populating ALL_ANATOMY_CONTAINS_NEW
    end;

    begin

      define anatid like anatomy_item.anatitem_zdb_id;
      define stgid like stage.stg_zdb_id;
      define nSynonyms int;
      define nGenesForThisItem int;
      define nGenesForChildItems int;
      define nDistinctGenes int;

      create temp table genes_with_xpats
	(
	  gene_zdb_id	varchar(50)
	);

      -- ----------------------------------------------------------------------------
      -- begin populating ANATOMY_STATS_NEW
      -- ----------------------------------------------------------------------------

      foreach
	select anatitem_zdb_id
	  into anatid
	  from anatomy_item

	-- get # of synonyms the anatomy item has.

	select count(*)
	  into nSynonyms
	  from data_alias
	  where dalias_data_zdb_id = anatid;

	-- get list of genes that have expression patterns for this
	-- anatomy item

	insert into genes_with_xpats
	  select distinct xpat_gene_zdb_id
	    from expression_pattern, expression_pattern_anatomy
	    where xpatanat_anat_item_zdb_id = anatid 
	      and xpatanat_xpat_zdb_id = xpat_zdb_id;

	let nGenesForThisItem = DBINFO('sqlca.sqlerrd2');

	-- get list of genes that have expression patterns for this
	-- anatomy item's children

	insert into genes_with_xpats
	  select distinct xpat_gene_zdb_id
	    from all_anatomy_contains_new,
		 expression_pattern, expression_pattern_anatomy
	    where allanatcon_contained_zdb_id = xpatanat_anat_item_zdb_id
	      and allanatcon_container_zdb_id = anatid
	      and xpatanat_xpat_zdb_id = xpat_zdb_id;

	let nGenesForChildItems = DBINFO('sqlca.sqlerrd2');

	select count (distinct gene_zdb_id)
	  into nDistinctGenes
	  from genes_with_xpats;

	insert into anatomy_stats_new
	    ( anatstat_anatitem_zdb_id, anatstat_object_type, 
	      anatstat_synonym_count,
	      anatstat_anatitem_count, anatstat_contains_count, 
	      anatstat_total_distinct_count )
	  values 
	    ( anatid, 'GENE', nSynonyms, nGenesForThisItem,
	      nGenesForChildItems, nDistinctGenes ) ;

	delete from genes_with_xpats;
	
      end foreach;

      -- done populating ANATOMY_STATS_NEW

      -- ----------------------------------------------------------------------------
      -- begin populating ANATOMY_STAGE_STATS_NEW
      -- ----------------------------------------------------------------------------

      foreach
	select allanatstg_anat_item_zdb_id, allanatstg_stg_zdb_id
	  into anatid, stgid
	  from all_anatomy_stage_new

	-- get list of genes that have expression patterns for this
	-- anatomy item in this stage 

	insert into genes_with_xpats
	  select distinct xpat_gene_zdb_id
	    from expression_pattern, 
		 expression_pattern_anatomy, stage as start, stage as end, 
		 stage as mystage
	    where xpatanat_anat_item_zdb_id = anatid 
	      and xpatanat_xpat_zdb_id = xpat_zdb_id
	      and start.stg_zdb_id = xpatanat_xpat_start_stg_zdb_id
	      and end.stg_zdb_id = xpatanat_xpat_end_stg_zdb_id
	      and mystage.stg_zdb_id = stgid
	      and mystage.stg_hours_start >= start.stg_hours_start
	      and mystage.stg_hours_end <= end.stg_hours_end;

	let nGenesForThisItem = DBINFO('sqlca.sqlerrd2');

	-- get list of genes that have expression patterns for this
	-- anatomy item's children in this stage 

	insert into genes_with_xpats
	  select distinct xpat_gene_zdb_id
	    from all_anatomy_contains_new,
		 expression_pattern, expression_pattern_anatomy,
		 stage as start, stage as end, stage as mystage
	    where allanatcon_contained_zdb_id = xpatanat_anat_item_zdb_id
	      and allanatcon_container_zdb_id = anatid
	      and xpatanat_xpat_zdb_id = xpat_zdb_id
	      and start.stg_zdb_id = xpatanat_xpat_start_stg_zdb_id
	      and end.stg_zdb_id = xpatanat_xpat_end_stg_zdb_id
	      and mystage.stg_zdb_id = stgid
	      and mystage.stg_hours_start >= start.stg_hours_start
	      and mystage.stg_hours_end <= end.stg_hours_end;

	let nGenesForChildItems = DBINFO('sqlca.sqlerrd2');

	select count (distinct gene_zdb_id)
	  into nDistinctGenes
	  from genes_with_xpats;

	insert into anatomy_stage_stats_new
	    ( anatstgstat_anatitem_zdb_id, anatstgstat_stg_zdb_id, 
	      anatstgstat_object_type, anatstgstat_anatitem_stg_count, 
	      anatstgstat_contains_count, anatstgstat_total_distinct_count )
	  values 
	    ( anatid, stgid,
	      'GENE', nGenesForThisItem,
	      nGenesForChildItems, nDistinctGenes ) ;

	delete from genes_with_xpats;
	
      end foreach;

      -- done populating ANATOMY_STAGE_STATS_NEW

    end


    -- ----------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- ----------------------------------------------------------------------------

    begin work;


    -- Delete the old tables.  Some may not exist (if the DB has just
    -- been created), so ignore errors from the drops.

    begin -- local exception handler for dropping of original tables

      on exception in (-206)
	      -- ignore any table that doesn't already exist
      end exception with resume;

      drop table stage_items_contained;
      drop table stage_item_child_list;

      drop table anatomy_stats;
      drop table anatomy_stage_stats;
      drop table anatomy_display;
      drop table all_anatomy_stage;
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


      -- ---- ALL_ANATOMY_STAGE ----

      rename table all_anatomy_stage_new to all_anatomy_stage;

      -- primary key

      let errorHint = "all_anatomy_stage_primary_key_index";
      drop index all_anatomy_stage_new_primary_key_index;
      create unique index all_anatomy_stage_primary_key_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id,
			      allanatstg_stg_zdb_id)
	fillfactor 100
        in idxdbs1;

      alter table all_anatomy_stage add constraint
        primary key (allanatstg_anat_item_zdb_id, allanatstg_stg_zdb_id)
	constraint all_anatomy_stage_primary_key;

      -- foreign keys

      -- Use ON DELETE CASCADE in every foreign key clause.  This means we
      -- can do maintenance on the base tables and know that related
      -- records in the fast search tables have also been dropped.

      let errorHint = "allanatstg_anat_item_zdb_id_index";
      create index allanatstg_anat_item_zdb_id_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table all_anatomy_stage add constraint
        foreign key (allanatstg_anat_item_zdb_id)
	references anatomy_item 
	  on delete cascade
	constraint allanatstg_anat_item_zdb_id_foreign_key;

      let errorHint = "allanatstg_stg_zdb_id_index";
      drop index allanatstg_new_stg_zdb_id_index;
      create index allanatstg_stg_zdb_id_index
        on all_anatomy_stage (allanatstg_stg_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table all_anatomy_stage add constraint
        foreign key (allanatstg_stg_zdb_id)
	references stage
	  on delete cascade
	constraint allanatstg_stg_zdb_id_foreign_key;


       	
      -- ---- ANATOMY_DISPLAY ----

      rename table anatomy_display_new to anatomy_display;

      -- primary key

      create unique index anatomy_display_primary_key_index
        on anatomy_display (anatdisp_hier_code,
			    anatdisp_stg_zdb_id,
			    anatdisp_seq_num)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_display add constraint 
        primary key (anatdisp_hier_code, anatdisp_stg_zdb_id, anatdisp_seq_num)
	constraint anatomy_display_primary_key;

      -- foreign keys

      let errorHint = "anatdisp_hier_code_index";
      create index anatdisp_hier_code_index
        on anatomy_display (anatdisp_hier_code)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_display add constraint
        foreign key (anatdisp_hier_code)
	references anatomy_hierarchy
	  on delete cascade
	constraint anatdisp_hier_code_foreign_key;

      let errorHint = "anatdisp_stg_zdb_id_index";
      create index anatdisp_stg_zdb_id_index
        on anatomy_display (anatdisp_stg_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_display add constraint
        foreign key (anatdisp_stg_zdb_id)
	references stage
	  on delete cascade
	constraint anatdisp_stg_zdb_id_foreign_key;

      let errorHint = "anatdisp_item_zdb_id_index";
      create index anatdisp_item_zdb_id_index
        on anatomy_display (anatdisp_item_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_display add constraint
        foreign key (anatdisp_item_zdb_id)
	references anatomy_item 
	  on delete cascade
	constraint anatdisp_item_zdb_id_foreign_key;

      let errorHint = "anatdisp_item_name_index";
      create index anatdisp_item_name_index
        on anatomy_display (anatdisp_item_name)
	fillfactor 100
	in idxdbs1;


      
      	
      -- ---- ANATOMY_STATS ----

      rename table anatomy_stats_new to anatomy_stats;

      -- primary key

      let errorHint = "anatomy_stats_primary_key_index";
      create unique index anatomy_stats_primary_key_index
        on anatomy_stats (anatstat_anatitem_zdb_id,
			  anatstat_object_type)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_stats add constraint
        primary key (anatstat_anatitem_zdb_id,
		     anatstat_object_type)
	constraint anatomy_stats_primary_key;

      -- foreign keys

      let errorHint = "anatstat_anatitem_zdb_id_index";
      create index anatstat_anatitem_zdb_id_index
        on anatomy_stats (anatstat_anatitem_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_stats add constraint
        foreign key (anatstat_anatitem_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint anatstat_anatitem_zdb_id_foreign_key;

      
      
      -- ---- ANATOMY_STAGE_STATS ----

      rename table anatomy_stage_stats_new to anatomy_stage_stats;

      -- primary key

      let errorHint = "anatomy_stage_stats_primary_key_index";
      create unique index anatomy_stage_stats_primary_key_index
        on anatomy_stage_stats (anatstgstat_anatitem_zdb_id,
			        anatstgstat_stg_zdb_id,
				anatstgstat_object_type)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_stage_stats add constraint
        primary key (anatstgstat_anatitem_zdb_id,
		     anatstgstat_stg_zdb_id,
		     anatstgstat_object_type)
	constraint anatomy_stage_stats_primary_key;

      -- foreign keys

      let errorHint = "anatstgstat_anatitem_zdb_id_index";
      create index anatstgstat_anatitem_zdb_id_index
        on anatomy_stage_stats (anatstgstat_anatitem_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_stage_stats add constraint
        foreign key (anatstgstat_anatitem_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint anatstgstat_anatitem_zdb_id_foreign_key;

      let errorHint = "anatstgstat_stg_zdb_id_index";
      create index anatstgstat_stg_zdb_id_index
        on anatomy_stage_stats (anatstgstat_stg_zdb_id)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_stage_stats add constraint
        foreign key (anatstgstat_stg_zdb_id)
	references stage
	  on delete cascade
	constraint anatstgstat_stg_zdb_id_foreign_key;

      
      
      -- ---- ALL_ANATOMY_CONTAINS ----

      rename table all_anatomy_contains_new to all_anatomy_contains;

      -- primary key

      let errorHint = "all_anatomy_contains_primary_key_index";
      drop index all_anatomy_contains_new_primary_key_index;
      create unique index all_anatomy_contains_primary_key_index
        on all_anatomy_contains (allanatcon_container_zdb_id,     
				 allanatcon_contained_zdb_id)
	fillfactor 100
	in idxdbs2;
      alter table all_anatomy_contains add constraint
        primary key (allanatcon_container_zdb_id,     
		     allanatcon_contained_zdb_id)
	constraint all_anatomy_contains_primary_key;

      -- foreign keys

      let errorHint = "allanatcon_container_zdb_id_index";
      create index allanatcon_container_zdb_id_index
        on all_anatomy_contains (allanatcon_container_zdb_id)
	fillfactor 100
	in idxdbs2;
      alter table all_anatomy_contains add constraint
        foreign key (allanatcon_container_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint allantcon_container_zdb_id_foreign_key;

      let errorHint = "allanatcon_contained_zdb_id_index";
      create index allanatcon_contained_zdb_id_index
        on all_anatomy_contains (allanatcon_contained_zdb_id)
	fillfactor 100
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
  update statistics high for table all_anatomy_stage;
  update statistics high for table anatomy_display;
  update statistics high for table anatomy_stats;
  update statistics high for table anatomy_stage_stats;
  update statistics high for table all_anatomy_contains;
  commit work;

  update zdb_flag set zflag_is_on = "f"
	where zflag_name = "regen_anatomy";
	  
  update zdb_flag set zflag_last_modified = CURRENT
	where zflag_name = "regen_anatomy";

  return 0;

end function;

grant execute on function "informix".regen_anatomy () 
  to "public" as "informix";
  
update statistics for function regen_anatomy;