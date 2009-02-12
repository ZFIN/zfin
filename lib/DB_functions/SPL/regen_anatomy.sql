
-- Estimated Run Time: 3 hours

-- Drop all out-of-date functions.
drop function regen_anatomy;
drop function populate_all_anatomy_contains;
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
  -- to populate anatomy_display_new table by using the intermedia table 
  -- stage_item_contained and stage_item_child_list.

  define childIndent int;
  define childId like anatomy_item.anatitem_zdb_id;
  define childName like anatomy_item.anatitem_name;
  define childNameOrder like anatomy_item.anatitem_name_order;

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
  -- would only have one display with it highest leve at a particular stage. 
  foreach
     select sic_anatitem_zdb_id, anatitem_name, anatitem_name_order
       into childId, childName, childNameOrder
       from anatomy_relationship, stage_items_contained, anatomy_item
      where parentId = anatrel_anatitem_1_zdb_id
        and sic_anatitem_zdb_id = anatrel_anatitem_2_zdb_id
	and sic_anatitem_zdb_id = anatitem_zdb_id

    insert into stage_item_child_list 
         values(parentId,childId,childName, childNameOrder);

    delete from stage_items_contained where sic_anatitem_zdb_id = childId;

   end foreach 
  
   -- For each direct descendant saved in stage_item_child_list, recursively
   -- call the function to populate furthur descendant.
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
    where anatrel_dagedit_id in ('is_a','part_of');

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
	  and a.anatrel_dagedit_id in ('is_a', 'part_of')
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
  --  anatomy_stats: term's gene count and synonyms for gene count for
			--expression patterns and term's genotype count
			--and synonyms count for phenotype.
  --  anatomy_stage_stats: terms gene count and synonyms count of a certain stage
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
	references anatomy_item
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
	references anatomy_item
	constraint stimchilis_item_zdb_id_foreign_key;

      create index stimchilis_child_zdb_id_index
        on stage_item_child_list (stimchilis_child_zdb_id)
	in idxdbs1;
      alter table stage_item_child_list add constraint
        foreign key (stimchilis_child_zdb_id)
	references anatomy_item
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



      -- ---- ANATOMY_STATS ----

      let errorHint = "Creating anatomy_stats_new";
      if (exists (select *
	           from systables
		   where tabname = "anatomy_stats_new")) then
	drop table anatomy_stats_new;
      end if

      create table anatomy_stats_new 
	(
	  anatstat_anatitem_zdb_id          varchar(50),
	  anatstat_object_type              varchar(32)
             not null,
	  anatstat_synonym_count	    integer
	     not null,
	  anatstat_object_count           integer
	     not null,
	  anatstat_contains_object_count           integer
	     not null,
	  anatstat_total_distinct_count     integer
	     not null
        )
	in tbldbs2
	extent size 128 next size 128 
	lock mode page;


      -- ---- ANATOMY_STAGE_STATS ----

--      let errorHint = "Creating anatomy_stage_stats_new";
--      if (exists (select *
--	           from systables
--		   where tabname = "anatomy_stage_stats_new")) then
--	drop table anatomy_stage_stats_new;
--      end if
--
--      create table anatomy_stage_stats_new 
--	(
--	  anatstgstat_anatitem_zdb_id          varchar(50),
--	  anatstgstat_stg_zdb_id                varchar(50),
--	  anatstgstat_object_type               char(32),
--	  anatstgstat_object_count       integer
--	     not null,
--	  anatstgstat_contains_object_count            integer
--	     not null,
--	  anatstgstat_total_distinct_count               integer
--	     not null
 --       )
--	fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
--	extent size 256 next size 256 
--	lock mode page;


      -- ---- ALL_ANATOMY_CONTAINS ----
      let errorHint = "Creating all_anatomy_contains_new";
      -- this table stores every anatomy term with every ancestor 
      -- that has a contains relationship and the shortest distance 
      -- between each pair.  In this case, a contains relationship
      -- is being defined as dagedit_id's "is_a" and "part_of", which
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

      -- ---------------------------------------------------
      --    ALL_ANATOMY_STAGE
      -- ---------------------------------------------------
      let errorhint = "Populating all_anatomy_stage";

      -- For each anatomy_item_zdb_id, find all stages this item occurs in.

      -- if the start stage of an anatomy item is Unknown, only 
      -- insert then end stage, and vice verse.
      insert into all_anatomy_stage
    	select anatitem_end_stg_zdb_id, anatitem_zdb_id
	  from anatomy_item, stage
	 where anatitem_start_stg_zdb_id = stg_zdb_id
           and stg_name = "Unknown";
  
      insert into all_anatomy_stage
    	select anatitem_start_stg_zdb_id, anatitem_zdb_id
	  from anatomy_item, stage
	 where anatitem_end_stg_zdb_id = stg_zdb_id
           and stg_name = "Unknown";

      -- for all other anatitems find stages it is contained in
      insert into all_anatomy_stage
    	select s1.stg_zdb_id, anatitem_zdb_id
	  from stage s1, anatomy_item
	  where anatitem_name_lower <> 'not specified'
	    and s1.stg_name <> 'Unknown'
	    and exists 
		(  select *
		     from stage ss, stage se
		    where anatitem_start_stg_zdb_id = ss.stg_zdb_id
		      and ss.stg_name <> "Unknown"
		      and anatitem_end_stg_zdb_id = se.stg_zdb_id      
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
      -- stage_id to insert associated anatomy_items. 

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

	foreach 
      	     select crt.sic_anatitem_zdb_id, anatitem_name, stg_hours_start 
               into anatomyId, anatomyName, stgHoursStart
               from stage_items_contained crt, anatomy_item, stage
      	      where crt.sic_anatitem_zdb_id = anatitem_zdb_id
	        and anatitem_start_stg_zdb_id = stg_zdb_id
 	        and not exists
		  ( select * 
	    	      from anatomy_relationship, stage_items_contained prt
	             where prt.sic_anatitem_zdb_id = anatrel_anatitem_1_zdb_id
	     	       and crt.sic_anatitem_zdb_id = anatrel_anatitem_2_zdb_id    
		   )
            order by stg_hours_start, anatitem_name 

      	    execute function populate_anat_display_stage_children(
        		stageId, anatomyId, anatomyName, indent, seqNum) 
            into seqNum;

        end foreach

        -- clean up the stage specific tables for the next iteration
        delete from stage_items_contained;
        delete from stage_item_child_list;

      end foreach


      -- -----------------------------------------------------------------------
      --     ALL_ANATOMY_CONTAINS_NEW
      -- -----------------------------------------------------------------------

      let errorHint = "Populating all_anatomy_contains_new";

      execute function populate_all_anatomy_contains()
        into nRows;

      update statistics high for table all_anatomy_contains_new;

      
    
      -- ---------------------------------------------------
      --     ANATOMY_STATS_NEW
      -- ---------------------------------------------------
      let errorhint = "Populating anatomy_stats_new";

      -- a temp table to hold genes that expressed in an anatomy term
      -- and all its child terms, and to count the distinct number.
      create temp table genes_with_xpats
	(
	  gene_zdb_id	varchar(50)
	);

      foreach
	select anatitem_zdb_id
	  into anatomyId
	  from anatomy_item

	-- get # of synonyms the anatomy item has.

	select count(*)
	  into nSynonyms
	  from data_alias
	  where dalias_data_zdb_id = anatomyId
	    and dalias_group <> 'secondary id';

	-- get list of genes that have expression patterns for this
	-- anatomy item

	insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id
	    from expression_experiment, outer marker probe, marker gene, expression_result
	    where xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id
              and xpatex_gene_zdb_id = gene.mrkr_zdb_id
              and xpatres_anat_item_zdb_id = anatomyId 
	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
	      and xpatres_expression_found = 't'
              and gene.mrkr_abbrev[1,10] <> "WITHDRAWN:"
              and probe.mrkr_abbrev[1,10] <> "WITHDRAWN:"
          and not exists(
              select 'x' from clone
              where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id
              and clone_problem_type = 'Chimeric'
          ) ;

	let nGenesForThisItem = DBINFO('sqlca.sqlerrd2');

	-- get list of genes that have expression patterns for this
	-- anatomy item's children

	insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id
	    from all_anatomy_contains_new,
		 expression_experiment, outer marker probe, marker gene, expression_result
	    where xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id
              and xpatex_gene_zdb_id = gene.mrkr_zdb_id
              and allanatcon_contained_zdb_id = xpatres_anat_item_zdb_id
	      and allanatcon_container_zdb_id = anatomyId
	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
	      and xpatres_expression_found = 't'
              and gene.mrkr_abbrev[1,10] <> "WITHDRAWN:"
              and probe.mrkr_abbrev[1,10] <> "WITHDRAWN:"
         and not exists(
             select 'x' from clone
             where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id
             and clone_problem_type = 'Chimeric'
         )
              ;

	let nGenesForChildItems = DBINFO('sqlca.sqlerrd2');

	select count (distinct gene_zdb_id)
	  into nDistinctGenes
	  from genes_with_xpats;

	insert into anatomy_stats_new
	    ( anatstat_anatitem_zdb_id, anatstat_object_type, 
	      anatstat_synonym_count,
	      anatstat_object_count, anatstat_contains_object_count, 
	      anatstat_total_distinct_count )
	  values 
	    ( anatomyId, 'GENE', nSynonyms, nGenesForThisItem,
	      nGenesForChildItems, nDistinctGenes ) ;

	delete from genes_with_xpats;
	
      end foreach;

      let errorhint = "Populating anatomy_stats_new with genotypes and related phenotypes instead of genes and expression patterns";

      -- a temp table to hold genotypes that have phenotype in an anatomy term
      -- and all its child terms, and to count the distinct number.

      create temp table genos_with_phenos
	(
	  geno_zdb_id	varchar(50)
	);

      foreach
	select anatitem_zdb_id
	  into anatomyId
	  from anatomy_item

	-- get # of synonyms the anatomy item has.

	select count(*)
	  into nSynonyms
	  from data_alias
	  where dalias_data_zdb_id = anatomyId
	    and dalias_group <> 'secondary id';

	-- get list of genes that have expression patterns for this
	-- anatomy item. Suppress wildtype genos in this list.

	insert into genos_with_phenos
	  select distinct genox_geno_Zdb_id
	    from atomic_phenotype, genotype_experiment, genotype
	    where (apato_subterm_zdb_id = anatomyId or
		   apato_superterm_zdb_id = anatomyId)
	      and genox_zdb_id = apato_genox_zdb_id
	      and genox_geno_zdb_id = geno_zdb_id
	      and geno_is_wildtype = 'f';

	let nGenosForThisItem = DBINFO('sqlca.sqlerrd2');

	-- get list of genes that have expression patterns for this
	-- anatomy item's children. Suppress wildtype genos.

	insert into genos_with_phenos
	  select distinct genox_geno_zdb_id
	    from all_anatomy_contains_new,
		 atomic_phenotype, genotype_Experiment, genotype
	    where (allanatcon_contained_zdb_id = apato_subterm_zdb_id or
			allanatcon_contained_zdb_id = apato_superterm_zdb_id)
	      and allanatcon_container_zdb_id = anatomyId
	      and apato_genox_Zdb_id = genox_Zdb_id
	      and genox_geno_zdb_id = geno_zdb_id
	      and geno_is_wildtype = 'f';

	let nGenosForChildItems = DBINFO('sqlca.sqlerrd2');

	select count (distinct geno_zdb_id)
	  into nDistinctGenos
	  from genos_with_phenos;

	insert into anatomy_stats_new
	    ( anatstat_anatitem_zdb_id, anatstat_object_type,
	      anatstat_synonym_count,
	      anatstat_object_count, anatstat_contains_object_count,
	      anatstat_total_distinct_count )
	  values
	    ( anatomyId, 'GENO', nSynonyms, nGenosForThisItem,
	      nGenosForChildItems, nDistinctGenos ) ;

	delete from genos_with_phenos;

      end foreach;


      -- -----------------------------------------------------------------------
      --    ANATOMY_STAGE_STATS_NEW
      -- -----------------------------------------------------------------------


--      let errorHint = "Populating anatomy_stage_stats_new";
--
--      foreach
--	select allanatstg_anat_item_zdb_id, allanatstg_stg_zdb_id
--	  into anatomyId, stageId
--	  from all_anatomy_stage

	-- get list of genes that have expression patterns for this
	-- anatomy item in this stage 

--	insert into genes_with_xpats
--	  select distinct xpatex_gene_zdb_id
--	    from expression_experiment, expression_result, marker
--	    where xpatres_anat_item_zdb_id = anatomyId 
--	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
--	      and stg_windows_overlap(stageId,stageId,xpatres_start_stg_zdb_id,xpatres_end_stg_zdb_id)
--	      and xpatres_expression_found = 't'
--	      and xpatex_gene_zdb_Id = mrkr_zdb_id
--	      and mrkr_name not like "WITHDRAWN%";

--	let nGenesForThisItem = DBINFO('sqlca.sqlerrd2');

	-- get list of genes that have expression patterns for this
	-- anatomy item's children in this stage 

--	insert into genes_with_xpats
--	  select distinct xpatex_gene_zdb_id
--	    from all_anatomy_contains_new,
--		 expression_experiment, expression_result,marker
--	    where allanatcon_contained_zdb_id = xpatres_anat_item_zdb_id
--	      and allanatcon_container_zdb_id = anatomyId
--	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
--	      and stg_windows_overlap(stageId,stageId,xpatres_start_stg_zdb_id,xpatres_end_stg_zdb_id)
--	      and xpatres_expression_found = 't'
--	      and xpatex_gene_zdb_Id = mrkr_zdb_id
--	      and mrkr_name not like "WITHDRAWN%";

--	let nGenesForChildItems = DBINFO('sqlca.sqlerrd2');

--	select count (distinct gene_zdb_id)
--	  into nDistinctGenes
--	  from genes_with_xpats;

--	insert into anatomy_stage_stats_new
--	    ( anatstgstat_anatitem_zdb_id, anatstgstat_stg_zdb_id, 
--	      anatstgstat_object_type, anatstgstat_object_count, 
--	      anatstgstat_contains_object_count, anatstgstat_total_distinct_count )
--	  values 
--	    ( anatomyId, stageId,
--	      'GENE', nGenesForThisItem,
--	      nGenesForChildItems, nDistinctGenes ) ;
--
--	delete from genes_with_xpats;
--
--	
--     end foreach;




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

      drop table anatomy_stats;
--      drop table anatomy_stage_stats;
      drop table anatomy_display;
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

 
      -- ---- ANATOMY_DISPLAY ----

      rename table anatomy_display_new to anatomy_display;

      -- primary key

      create unique index anatomy_display_primary_key_index
        on anatomy_display (anatdisp_stg_zdb_id,
			    anatdisp_seq_num)
	fillfactor 100
	in idxdbs1;
      alter table anatomy_display add constraint 
        primary key (anatdisp_stg_zdb_id, anatdisp_seq_num)
	constraint anatomy_display_primary_key;

      -- foreign keys

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

--      rename table anatomy_stage_stats_new to anatomy_stage_stats;

      -- primary key

--      let errorHint = "anatomy_stage_stats_primary_key_index";
--      create unique index anatomy_stage_stats_primary_key_index
--        on anatomy_stage_stats (anatstgstat_anatitem_zdb_id,
--			        anatstgstat_stg_zdb_id,
--				anatstgstat_object_type)
--	fillfactor 100
--	in idxdbs1;
--     alter table anatomy_stage_stats add constraint
--      primary key (anatstgstat_anatitem_zdb_id,
--		     anatstgstat_stg_zdb_id,
--		     anatstgstat_object_type)
--	constraint anatomy_stage_stats_primary_key;

      -- foreign keys

--      let errorHint = "anatstgstat_anatitem_zdb_id_index";
--      create index anatstgstat_anatitem_zdb_id_index
--        on anatomy_stage_stats (anatstgstat_anatitem_zdb_id)
--	fillfactor 100
--	in idxdbs1;
--      alter table anatomy_stage_stats add constraint
--        foreign key (anatstgstat_anatitem_zdb_id)
--	references anatomy_item
--	  on delete cascade
--	constraint anatstgstat_anatitem_zdb_id_foreign_key;

--      let errorHint = "anatstgstat_stg_zdb_id_index";
--      create index anatstgstat_stg_zdb_id_index
--        on anatomy_stage_stats (anatstgstat_stg_zdb_id)
--	fillfactor 100
--	in idxdbs1;
--      alter table anatomy_stage_stats add constraint
--        foreign key (anatstgstat_stg_zdb_id)
--	references stage
--	  on delete cascade
--	constraint anatstgstat_stg_zdb_id_foreign_key;

      
      
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
  update statistics high for table anatomy_display;
  update statistics high for table anatomy_stats;
--  update statistics high for table anatomy_stage_stats;
  update statistics high for table all_anatomy_contains;
  commit work;

  --   RELEASE ZDB_FLAG

  if release_zdb_flag("regen_anatomy") <> 0 then
    return 1;
  end if

  return 0;

end function;

grant execute on function "informix".regen_anatomy () 
  to "public" as "informix";
  
