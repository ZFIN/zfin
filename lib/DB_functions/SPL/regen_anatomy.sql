
-- Drop all out-of-date functions.
drop function regen_anatomy;
drop function populate_all_anatomy_contains;
drop procedure populate_anat_display_stage;
drop function populate_anat_display_stage_children;
drop procedure distinct_item_expression_count;



-- Create recursive functions in reverse order of execution.

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
  define child_id varchar(50);
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
    select anatcon_contained_zdb_id, anatitem_type_code, anatitem_name
      into child_id, hier_code, anatomy_name
      from anatomy_item, anatomy_contains, stage_items_contained
      where parent_id = anatcon_container_zdb_id
        and anatitem_zdb_id = anatcon_contained_zdb_id
        and anatitem_zdb_id = sic_anatitem_zdb_id
        and sic_stg_zdb_id = stage_id
      order by anatitem_name

    insert into stage_item_child_list 
      values(stage_id,parent_id,child_id,anatomy_name,hier_code);

    delete from stage_items_contained where sic_anatitem_zdb_id = child_id;

  end foreach -- retrieval of children

  foreach
    select stimchilis_child_zdb_id, stimchilis_anat_name, stimchilis_hier_code
      into child_id, anatomy_name, hier_code
      from stage_item_child_list
      where stimchilis_stg_zdb_id = stage_id
        and stimchilis_item_zdb_id = parent_id

    execute function populate_anat_display_stage_children( 
        child_id,child_indent,seq_num,hier_code,stage_id,anatomy_name ) 
      into seq_num;	

  end foreach

  return seq_num;
end function;



create procedure populate_anat_display_stage(stage_id varchar(50))

  -- For each stage, find the root anatitem(s) ie. the anatitems that don't
  -- have a parent in the same stage.  Execute populate...stage_children for
  -- each root anatitem.
  -- Called from regen_anatomy.

  define seqNum int;
  define indent int;
  define distance int;
  define hierCode, prevCode char(2);
  define anatomyId, anatomy_name varchar(50);
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
    select anatitem_zdb_id, anathier_code, anatitem_name, s.stg_hours_start
      into anatomyId, hierCode, anatomy_name, temp
      from stage s, stage_items_contained, anatomy_hierarchy, anatomy_item a1
      where sic_anatitem_zdb_id = a1.anatitem_zdb_id
        and anathier_code = a1.anatitem_type_code
	and a1.anatitem_start_stg_zdb_id = s.stg_zdb_id
	and not exists
	(
	  select * 
	  from stage_items_contained, anatomy_contains, anatomy_item a2
	  where a1.anatitem_zdb_id = anatcon_contained_zdb_id
	    and a2.anatitem_zdb_id = anatcon_container_zdb_id 
	    and sic_anatitem_zdb_id = anatcon_container_zdb_id
	)
      order by anathier_code, anatitem_name

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



create procedure distinct_item_expression_count(aid varchar(50),
						sid varchar(50),
						ind integer,seq integer)
  -- called from regen_anatomy.

  define xid varchar(50);
  define no_children integer;
  define count integer;
  define child_count integer;
  define total_count integer;
  define next_seq integer;
  define child varchar(50);
  define next_ind integer;
  define oid varchar(50);
  let oid = "XPAT";
  let child = "null";
  let next_seq = seq +1;
  let child_count = 0;
  let total_count = 0;
  let no_children = 0;

  let count = 0;

  foreach
    --order the children of the current item by seq_num
    --call the function again for each child
    select anatdispstg_item_zdb_id,anatdispstg_indent 
      into child, next_ind
      from anatomy_display_stage
      where anatdispstg_seq_num = next_seq
        and anatdispstg_indent > ind

    execute procedure distinct_item_expression_count(child,sid,next_ind,next_seq);

    let no_children = 1;

    --get all of the xpat_zdbs for the current item and 
    --insert them into all_item_x_p
    foreach
      select distinct sxa_xpat_zdb_id
        into xid
        from stg_xpat_anat
        where sxa_stg_zdb_id = sid
          and sxa_anat_item_zdb_id = aid

      insert into all_item_expression_pattern
        values(aid,xid,seq,ind);
      let count = count+1;
    end foreach

    foreach
      select distinct aixp_xpatanat_xpat_zdb_id
        into xid
        from all_item_expression_pattern
        where aixp_seq_num >= seq

      let total_count = total_count +1;
    end foreach

  end foreach;

  --the item didn't have any children, so do an item/stage count and then
  --call the next item in the stage if it is not the root of the next tree.
  if (no_children = 0) then
    --get all of the xpat_zdbs for the current item and 
    --insert them into all_item_x_p
    foreach
      select distinct sxa_xpat_zdb_id
        into xid
        from stg_xpat_anat
        where sxa_stg_zdb_id = sid
          and sxa_anat_item_zdb_id = aid

      insert into all_item_expression_pattern
        values(aid,xid,seq,ind);
      let count = count+1;
    end foreach

    let total_count = count;

    foreach
      select anatdispstg_item_zdb_id,anatdispstg_indent 
        into child, next_ind
        from anatomy_display_stage
        where anatdispstg_seq_num = next_seq
          and anatdispstg_indent != 1

      execute procedure distinct_item_expression_count(child, sid, next_ind,
						       next_seq);
    end foreach --next non-root item 
  end if --no children

  let child_count = total_count - count;
  insert into anatomy_stage_stats_new 
    values(aid,sid,oid,count,child_count,total_count);

end procedure;




create function populate_all_anatomy_contains()
  returning integer

  -- find the transitive closure of anatomy contains, 
  -- keeping only the closest ancestor

  -- called from regen_anatomy

  define dist int;
  define delta int;

  let dist = 1;
  let delta = -1;

  -- the first level is a gimmie from anatomy_contains
  -- also _all_ child nodes are explicitly listed
  -- so we only need to find ancestors of these child nodes
  insert into all_anatomy_contains_new
    select anatcon_containeR_zdb_id ,
	   anatcon_containeD_zdb_id, 
	   dist
      from anatomy_contains;

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
      select distinct a.anatcon_containeR_zdb_id,     -- A.ancestor
		      b.allanatcon_containeD_zdb_id,  -- B.child
		      dist                            -- min depth
	from anatomy_contains a,            -- source of all ancestors
             all_anatomy_contains_new b     -- source of all childs 

	where b.allanatcon_min_contain_distance = (dist - 1) 
	      -- limit the search to the previous level          
          and b.allanatcon_containeR_zdb_id = a.anatcon_containeD_zdb_id
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



--------------------------------------------------------------------


create dba function regen_anatomy()
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

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get rid of them, and leave the original tables around

	on exception in (-206, -255, -668)
          --  206: OK to get "Table not found" here, since we might
          --       not have created all tables at the time of the exception
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

	return 1;
      end
    end exception;

    --trace on;

    begin

      -- ======  CREATE TABLES THAT ONLY EXIST IN THIS FUNCTION  ======

      -- ---- NON_PARENT_STAGE ----

      if (exists (select *
	           from systables
		   where tabname = "non_parent_stage")) then
	drop table non_parent_stage;
      end if

      create table non_parent_stage
	(
	  zdb_id	varchar(50),
	  start_hour	decimal(7,2) 
	    not null
	      constraint non_parent_stage_start_hour_not_null,
	  end_hour	decimal(7,2)
	    not null
	      constraint non_parent_stage_end_hour_not_null
	)
	in zfindbs_b
	extent size 8 next size 8;

	-- primary key

	create unique index non_parent_stage_primary_key_index
	  on non_parent_stage (zdb_id)
	  in zfindbs_c;
	alter table non_parent_stage add constraint
	  primary key (zdb_id)
	  constraint non_parent_stage_primary_key;



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
	in zfindbs_b
	extent size 64 next size 64;

      -- primary key

      create unique index stage_items_contained_primary_key_index
	on stage_items_contained (sic_stg_zdb_id,sic_anatitem_zdb_id)
	in zfindbs_c;
      alter table stage_items_contained add constraint
	primary key (sic_stg_zdb_id,sic_anatitem_zdb_id)
	constraint stage_items_contained_primary_key;

      -- foreign keys

      create index sic_stg_zdb_id_index
	on stage_items_contained (sic_stg_zdb_id)
	in zfindbs_c;
      alter table stage_items_contained add constraint
	foreign key (sic_stg_zdb_id)
	references stage
	constraint sic_stg_zdb_id_foreign_key;

      create index sic_anatitem_zdb_id_index
	on stage_items_contained (sic_anatitem_zdb_id)
	in zfindbs_c;
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
	      constraint stimchilis_hier_code_not_null
	)
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 128 next size 128;

      -- primary key

      create unique index stage_item_child_list_primary_key_index
        on stage_item_child_list (stimchilis_stg_zdb_id,
				  stimchilis_item_zdb_id,
				  stimchilis_child_zdb_id)  
	in zfindbs_b;
      alter table stage_item_child_list add constraint
        primary key (stimchilis_stg_zdb_id,
		     stimchilis_item_zdb_id,
		     stimchilis_child_zdb_id)
        constraint stage_item_child_list_primary_key;

      -- foreign keys

      create index stimchilis_stg_zdb_id_index
        on stage_item_child_list (stimchilis_stg_zdb_id)
	in zfindbs_b;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_stg_zdb_id)
	references stage
	constraint stimchilis_stg_zdb_id_foreign_key;

      create index stimchilis_item_zdb_id_index
        on stage_item_child_list (stimchilis_item_zdb_id)
	in zfindbs_b;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_item_zdb_id)
	references anatomy_item
	constraint stimchilis_item_zdb_id_foreign_key;

      create index stimchilis_child_zdb_id_index
        on stage_item_child_list (stimchilis_child_zdb_id)
	in zfindbs_b;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_child_zdb_id)
	references anatomy_item
	constraint stimchilis_child_zdb_id_foreign_key;



      -- ---- XPAT_STG_ANAT ----

      --create the xpat_stg_anat table (XP x XPA x S)
      --this table will be used more effectively when stage counts are 
      --incorporated.

      if (exists (select *
	           from systables
		   where tabname = "xpat_stg_anat")) then
	drop table xpat_stg_anat;
      end if

      create table xpat_stg_anat
	(
	  xpatstganat_xpat_zdb_id		varchar(50),
	  xpatstganat_stg_zdb_id		varchar(50),
	  xpatstganat_anat_item_zdb_id		varchar(50)
	)
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 64 next size 64;

      -- other indexes

      create index xpatstganat_xpat_zdb_id_index
        on xpat_stg_anat(xpatstganat_xpat_zdb_id)
	in zfindbs_c;

      create index xpatstganat_stg_zdb_id_index 
        on xpat_stg_anat(xpatstganat_stg_zdb_id)
	in zfindbs_c;

      create index xpatstganat_anat_item_zdb_id_index 
        on xpat_stg_anat(xpatstganat_anat_item_zdb_id)
	in zfindbs_c;


      -- ---- STG_XPAT_ANAT ----

      --this table contains a subset of xpat_stg_anat pertaining to one stage

      if (exists (select *
	           from systables
		   where tabname = "stg_xpat_anat")) then
	drop table stg_xpat_anat;
      end if

      create table stg_xpat_anat 
	(
	  sxa_xpat_zdb_id		varchar(50),
	  sxa_stg_zdb_id		varchar(50),
	  sxa_anat_item_zdb_id		varchar(50)
	)
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 64 next size 64;

      -- other indexes

      create index sxa_xpat_zdb_id_index 
        on stg_xpat_anat(sxa_xpat_zdb_id)
	in zfindbs_b;

      create index sxa_stg_zdb_id_index 
        on stg_xpat_anat(sxa_stg_zdb_id)
	in zfindbs_b;

      create index sxa_anat_item_zdb_id_index 
        on stg_xpat_anat(sxa_anat_item_zdb_id)
	in zfindbs_b;


      -- ---- ALL_ITEM_EXPRESSION_PATTERN ----

      if (exists (select *
	           from systables
		   where tabname = "all_item_expression_pattern")) then
	drop table all_item_expression_pattern;
      end if

      create table all_item_expression_pattern
	(
	  aixp_anatitem_zdb_id varchar(50),
	  aixp_xpatanat_xpat_zdb_id varchar(50),
	  aixp_seq_num integer,
	  aixp_indent integer
	)
	in zfindbs_b
	extent size 32 next size 32;



      -- ---- ANATOMY_DISPLAY_STAGE ----

      if (exists (select *
	           from systables
		   where tabname = "anatomy_display_stage")) then
	drop table anatomy_display_stage;
      end if

      create table anatomy_display_stage
	(
	  anatdispstg_hier_code char(2),
	  anatdispstg_stg_zdb_id varchar(50),
	  anatdispstg_seq_num integer,
	  anatdispstg_item_zdb_id varchar(50),
	  anatdispstg_indent integer
	)
	in zfindbs_b
	extent size 64 next size 64;



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
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 64 next size 64;

      -- create temporary indexes, these are dropped when table is renamed.

      create unique index all_anatomy_stage_new_primary_key_index
        on all_anatomy_stage_new (allanatstg_anat_item_zdb_id,
				  allanatstg_stg_zdb_id)
        in zfindbs_c;

      create index allanatstg_new_stg_zdb_id_index
        on all_anatomy_stage_new (allanatstg_stg_zdb_id)
	in zfindbs_c;


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
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 128 next size 128;

      -- create temporary indexes, these are dropped when table is renamed.

      create unique index anatomy_display_new_primary_key_index
        on anatomy_display_new (anatdisp_hier_code,
			        anatdisp_stg_zdb_id,
				anatdisp_seq_num)
	in zfindbs_c;

      create index anatdisp_new_stg_zdb_id_index
        on anatomy_display_new (anatdisp_stg_zdb_id)
	in zfindbs_c;


      -- ---- ANATOMY_STAGE_STATS ----

      if (exists (select *
	           from systables
		   where tabname = "anatomy_stage_stats_new")) then
	drop table anatomy_stage_stats_new;
      end if

      create table anatomy_stage_stats_new 
	(
	  anatstgstat_anat_item_zdb_id          varchar(50),
	  anatstgstat_stg_zdb_id                varchar(50),
	  anatstgstat_object_type               char(32),
	  anatstgstat_anat_item_stg_count       integer
	     not null,
	  anatstgstat_contains_count            integer
	     not null,
	  anatstgstat_total_count               integer
	     not null
        )
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 256 next size 256;


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
	fragment by round robin in zfindbs_a , zfindbs_b , zfindbs_c
	extent size 512 next size 512;

    end

    -- For each anatomy_item_zdb_id, find all stages the item occurs in,
    -- then insert the item and stage zdb_id's into all_anatomy stage.
    begin
      define stage_id varchar(50);
      define item_id varchar(50);
      define item_start_stg varchar(50);
      define item_end_stg varchar(50);
      define start_hour decimal(7,2);
      define end_hour decimal(7,2);

      --reduce query comparisons by using a table with only non-parent stages.
      foreach
	select s.stg_zdb_id, s.stg_hours_start, s.stg_hours_end
	  into stage_id, start_hour, end_hour
	  from stage s
	  where not exists 
		(
		  select *
		    from stage_contains
		    where s.stg_zdb_id = stgcon_container_zdb_id
		)

	insert into non_parent_stage 
	  values(stage_id,start_hour,end_hour);

      end foreach

      create index non_parent_stage_start_hour_index 
        on non_parent_stage(start_hour)
	in zfindbs_c;
      create index non_parent_stage_end_hour_index
        on non_parent_stage(end_hour)
	in zfindbs_c;


      --for each anatitem find all stages it is contained in

      foreach
	select s1.zdb_id, anatitem_zdb_id
	  into stage_id, item_id
	  from non_parent_stage s1, anatomy_item a1
	  where anatitem_name != 'structures'
	    and exists 
		(
		  select *
		    from non_parent_stage s2, non_parent_stage s3
		    where a1.anatitem_start_stg_zdb_id = s2.zdb_id
		      and a1.anatitem_end_stg_zdb_id = s3.zdb_id             
		      and (
			      (    s1.start_hour >= s2.start_hour
			       and s1.start_hour < s3.end_hour)
			   or
			      (    s1.end_hour > s2.start_hour
			       and s1.end_hour <= s3.end_hour)
			   or      
			      (    s1.start_hour <= s2.start_hour
			       and s1.end_hour >= s3.end_hour)
			 )           
		)

	 --insert the item/stage pair into all_anat_stg
	 insert into all_anatomy_stage_new 
	   values(item_id,stage_id);

      end foreach
    end

    begin
      -- Anatomy_display has variables that are stage based, so use each
      -- stage_id to insert associated anatomy_items. 

      define stage_ID varchar(50);

      foreach
	select s1.zdb_id 
	  into stage_ID 
	  from non_parent_stage s1

	execute procedure populate_anat_display_stage(stage_ID);

      end foreach

    end

    begin
      -- populate temp table
      define xpatid varchar(50);
      define stgid varchar(50);
      define anatid varchar(50);
      define startstgid varchar(50);
      define endstgid varchar(50);
      define hier_code char(2);
      define seq integer;
      define indent integer;
      define count integer;
      define child_count integer;
      define total_count integer;
      define objectid varchar(50);

      let objectid = "XPAT";

      --for each expression pattern
      foreach
	select xpat_zdb_id
	  into xpatid
	  from expression_pattern

	--retrieve stages contained in xpat stage range
	foreach
	  select s1.stg_zdb_id, xstrt.stg_zdb_id, xend.stg_zdb_id
	    into stgid,startstgid,endstgid 
	    from stage s1, stage xstrt, stage xend, expression_pattern_stage
	    where xpatstg_xpat_zdb_id = xpatid
	      and xpatstg_start_stg_zdb_id = xstrt.stg_zdb_id
	      and xpatstg_end_stg_zdb_id = xend.stg_zdb_id
	      and (
		       (    s1.stg_hours_start >= xstrt.stg_hours_start
			and s1.stg_hours_start < xend.stg_hours_end)
		   or
		       (    s1.stg_hours_end > xstrt.stg_hours_start
			and s1.stg_hours_end <= xend.stg_hours_end)
		   or      
		       (    s1.stg_hours_start <= xstrt.stg_hours_start
			and s1.stg_hours_end >= xend.stg_hours_end)
		   )  

	  --for each xpat_stage
	  --find all anat_items from xpatanat whose stage range overlaps the 
	  --current stg
	  foreach
	    select xpatanat_anat_item_zdb_id
	      into anatid
	      from expression_pattern_anatomy
	      where xpatanat_xpat_zdb_id = xpatid
		and xpatanat_xpat_start_stg_zdb_id = startstgid
		and xpatanat_xpat_end_stg_zdb_id = endstgid
		and exists 
		    (
		      select *
			from all_anatomy_stage_new
			where allanatstg_anat_item_zdb_id = xpatanat_anat_item_zdb_id
			  and allanatstg_stg_zdb_id = stgid
		    )  

	     --insert record into xpat_stg_anat(xpat_id,stg_id,anat_id)
	     insert into xpat_stg_anat 
	       values(xpatid,stgid,anatid);

	  end foreach;
	end foreach;
      end foreach;


      --trace on;


      --traverse the display table depth first approach with recursive function
      --for each stage in the xpat_stg table
      foreach
	select distinct anatdisp_stg_zdb_id
	  into stgid
	  from anatomy_display_new

	--store the xpat_stg_anat records for this stage in a temp table
	foreach
	  select xpatstganat_xpat_zdb_id,xpatstganat_anat_item_zdb_id
	    into xpatid,anatid
	    from xpat_stg_anat
	    where xpatstganat_stg_zdb_id = stgid

	  insert into stg_xpat_anat 
	    values(xpatid,stgid,anatid);
	end foreach

	--make a subset of anatomy_display for the current stage
	foreach
	  select anatdisp_hier_code, anatdisp_seq_num, anatdisp_item_zdb_id,
		 anatdisp_indent
	    into hier_code,seq,anatid,indent
	    from anatomy_display_new
	    where anatdisp_stg_zdb_id = stgid

	  insert into anatomy_display_stage
	    values (hier_code,stgid,seq,anatid,indent);
	end foreach

	--retrieve all items with indent = 1 and order by seq_num
	foreach
	  select anatdispstg_item_zdb_id,anatdispstg_seq_num,anatdispstg_indent
	    into anatid,seq,indent
	    from anatomy_display_stage
	    where anatdispstg_indent = 1
	    order by anatdispstg_seq_num

	  delete from all_item_expression_pattern;

	  --call function distinct_item_expression_count
	  execute procedure distinct_item_expression_count(anatid, stgid,
							   indent, seq);
	end foreach --item indent = 1

	delete from anatomy_display_stage;
      end foreach --stage

      --store a boolean value answering whether the item is contained in an
      --xpat
      select stg_zdb_id 
	into stgid 
	from stage 
	where stg_name = 'Any stage';
      foreach
	select a1.anatitem_zdb_id
	  into anatid
	  from anatomy_item a1
	  where exists(
		select *
		  from xpat_stg_anat
		  where xpatstganat_anat_item_zdb_id = a1.anatitem_zdb_id) 

	insert into anatomy_stage_stats_new 
	  values(anatid,stgid,objectid,1,0,1);
      end foreach

      foreach
	select a1.anatitem_zdb_id
	  into anatid
	  from anatomy_item a1
	  where not exists(
		select *
		  from xpat_stg_anat
		  where xpatstganat_anat_item_zdb_id = a1.anatitem_zdb_id) 

	insert into anatomy_stage_stats_new 
	  values(anatid,stgid,objectid,0,0,0);
      end foreach

    end

    -- populate all_anatomy_contains

    begin
      define nRows int;
      execute function populate_all_anatomy_contains()
        into nRows;
    end;


    --RENAME the new tables to REPLACE the old
    begin work;


    -- Delete the old tables.  Some may not exist (if the DB has just
    -- been created), so ignore errors from the drops.

    begin -- local exception handler for dropping of original tables

      on exception in (-206)
	      -- ignore any table that doesn't already exist
      end exception with resume;

      drop table non_parent_stage;
      drop table stage_items_contained;
      drop table stage_item_child_list;
      drop table anatomy_display_stage;
      drop table all_item_expression_pattern;
      drop table xpat_stg_anat;
      drop table stg_xpat_anat;

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

      drop index all_anatomy_stage_new_primary_key_index;
      create unique index all_anatomy_stage_primary_key_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id,
			      allanatstg_stg_zdb_id)
	fillfactor 100
        in zfindbs_c;

      alter table all_anatomy_stage add constraint
        primary key (allanatstg_anat_item_zdb_id, allanatstg_stg_zdb_id)
	constraint all_anatomy_stage_primary_key;

      -- foreign keys

      create index allanatstg_anat_item_zdb_id_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id)
	fillfactor 100
	in zfindbs_c;

      -- DO NOT include the foreign key clauses.  Allows us to
      -- do maintenance on the base tables without worrying about ripple
      -- effects in the fast search tables.

      { alter table all_anatomy_stage add constraint
        foreign key (allanatstg_anat_item_zdb_id)
	references anatomy_item on delete cascade
	constraint allanatstg_anat_item_zdb_id_foreign_key;
      }
      drop index allanatstg_new_stg_zdb_id_index;
      create index allanatstg_stg_zdb_id_index
        on all_anatomy_stage (allanatstg_stg_zdb_id)
	fillfactor 100
	in zfindbs_c;

      { alter table all_anatomy_stage add constraint
        foreign key (allanatstg_stg_zdb_id)
	references stage on delete cascade
	constraint allanatstg_stg_zdb_id_foreign_key;
      }

      -- ---- ANATOMY_DISPLAY ----

      rename table anatomy_display_new to anatomy_display;

      -- primary key

      drop index anatomy_display_new_primary_key_index;
      create unique index anatomy_display_primary_key_index
        on anatomy_display (anatdisp_hier_code,
			    anatdisp_stg_zdb_id,
			    anatdisp_seq_num)
	fillfactor 100
	in zfindbs_c;
      alter table anatomy_display add constraint 
        primary key (anatdisp_hier_code, anatdisp_stg_zdb_id, anatdisp_seq_num)
	constraint anatomy_display_primary_key;

      -- foreign keys

      create index anatdisp_hier_code_index
        on anatomy_display (anatdisp_hier_code)
	fillfactor 100
	in zfindbs_c;
      { alter table anatomy_display add constraint
        foreign key (anatdisp_hier_code)
	references anatomy_hierarchy 
	  on delete cascade
	constraint anatdisp_hier_code_foreign_key;
      }
      drop index anatdisp_new_stg_zdb_id_index;
      create index anatdisp_stg_zdb_id_index
        on anatomy_display (anatdisp_stg_zdb_id)
	fillfactor 100
	in zfindbs_c;
      { alter table anatomy_display add constraint
        foreign key (anatdisp_stg_zdb_id)
	references stage 
	  on delete cascade
	constraint anatdisp_stg_zdb_id_foreign_key;
      }
      create index anatdisp_item_zdb_id_index
        on anatomy_display (anatdisp_item_zdb_id)
	fillfactor 100
	in zfindbs_c;
      alter table anatomy_display add constraint
        foreign key (anatdisp_item_zdb_id)
	references anatomy_item 
	  on delete cascade
	constraint anatdisp_item_zdb_id_foreign_key;

      create index anatdisp_item_name_index
        on anatomy_display (anatdisp_item_name)
	fillfactor 100
	in zfindbs_c;
      { alter table anatomy_display add constraint
        foreign key (anatdisp_item_name)
	references anatomy_item (anatitem_name) 
	  on delete cascade
	constraint anatdisp_item_name_foreign_key;
      }

      -- ---- ANATOMY_STAGE_STATS ----

      rename table anatomy_stage_stats_new to anatomy_stage_stats;

      -- primary key

      create unique index anatomy_stage_stats_primary_key_index
        on anatomy_stage_stats (anatstgstat_anat_item_zdb_id,
			        anatstgstat_stg_zdb_id,
				anatstgstat_object_type)
	in zfindbs_c;
      alter table anatomy_stage_stats add constraint
        primary key (anatstgstat_anat_item_zdb_id,
		     anatstgstat_stg_zdb_id,
		     anatstgstat_object_type)
	constraint anatomy_stage_stats_primary_key;

      -- foreign keys

      create index anatstgstat_anat_item_zdb_id_index
        on anatomy_stage_stats (anatstgstat_anat_item_zdb_id)
	fillfactor 100
	in zfindbs_c;
      { alter table anatomy_stage_stats add constraint
        foreign key (anatstgstat_anat_item_zdb_id)
	references anatomy_item
	  on delete cascade
	constraint anatstgstat_anat_item_zdb_id_foreign_key;
      }
      create index anatstgstat_stg_zdb_id_index
        on anatomy_stage_stats (anatstgstat_stg_zdb_id)
	fillfactor 100
	in zfindbs_c;
      { alter table anatomy_stage_stats add constraint
        foreign key (anatstgstat_stg_zdb_id)
	references stage
	  on delete cascade
	constraint anatstgstat_stg_zdb_id_foreign_key;
      }

      -- ---- ALL_ANATOMY_CONTAINS ----

      rename table all_anatomy_contains_new to all_anatomy_contains;

      -- primary key

      create unique index all_anatomy_contains_primary_key_index
        on all_anatomy_contains (allanatcon_container_zdb_id,     
				 allanatcon_contained_zdb_id)
	fillfactor 100
	in zfindbs_c;
      alter table all_anatomy_contains add constraint
        primary key (allanatcon_container_zdb_id,     
		     allanatcon_contained_zdb_id)
	constraint all_anatomy_contains_primary_key;

      -- foreign keys

      create index allanatcon_container_zdb_id_index
        on all_anatomy_contains (allanatcon_container_zdb_id)
	fillfactor 100
	in zfindbs_c;
      { alter table all_anatomy_contains add constraint
        foreign key (allanatcon_container_zdb_id)
	references anatomy_item
	constraint allanatcon_container_zdb_id_foreign_key;
      }
      create index allanatcon_contained_zdb_id_index
        on all_anatomy_contains (allanatcon_contained_zdb_id)
	fillfactor 100
	in zfindbs_c;
      { alter table all_anatomy_contains add constraint
        foreign key (allanatcon_contained_zdb_id)
	references anatomy_item
	constraint allanatcon_contained_zdb_id_foreign_key;      
      }
    end -- Local exception handler

    commit work;

  end -- Global exception handler

  return 0;
end function;

