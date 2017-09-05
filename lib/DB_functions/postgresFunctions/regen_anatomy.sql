
-- REGEN_ANATOMY

create or replace function regen_anatomy()
  returns int as $log$

  -- populates anatomy fast search tables:
  --  anatomy_display: term's position in a dag display of a certain stage
  --  all_term_contains: each and every ancestor and descendant
  -- see regen_names.sql for details on how to debug SPL routines.

    declare stageId  stage.stg_zdb_id%TYPE;
     seqNum int;
     indent int;
     anatomyId  term.term_zdb_id%TYPE;
     anatomyName  term.term_name%TYPE;
     stgHoursStart  stage.stg_hours_start%TYPE;

     nRows int;	
    
     nSynonyms int;
     nGenesForThisItem int;
     nGenesForChildItems int;
     nDistinctGenes int;
     nGenosForThisItem int;
     nGenosForChildItems int;
     nDistinctGenos int;
     flagStatus int;

     begin 

     execute 'select grab_zdb_flag("regen_anatomy")' into flagStatus;

     if flagStatus > 0
       then 
       	    return 1;
     end if;


      -- =========  CREATE TABLES THAT ONLY EXIST IN THIS FUNCTION  =========

      -- ---- ALL_ANATOMY_STAGE ----

      -- a table contains every anatomy term and every stage the term is in. 
      -- the "Unknown" stage is excluded. 
      --
      -- This table is used to populate stage_items_contained. 

     drop table if exists all_anatomy_stage;
  

      create table all_anatomy_stage 
	(
	  allanatstg_stg_zdb_id	      text,
	  allanatstg_anat_item_zdb_id text
	);

      -- create temporary indexes, these are dropped when table is renamed.

      create unique index all_anatomy_stage_primary_key_index
        on all_anatomy_stage (allanatstg_anat_item_zdb_id,
				  allanatstg_stg_zdb_id);

      create index allanatstg_stg_zdb_id_index
        on all_anatomy_stage (allanatstg_stg_zdb_id);


      -- ---- STAGE_ITEMS_CONTAINED ----


      -- this table is populated/repopulated with all anatomy items existing
      -- at ONE stage a time. 
      -- It is used in the process of populating anatomy_display table.

	drop table if exists stage_items_contained;

      create table stage_items_contained
	(
	  sic_anatitem_zdb_id 	text,
	  primary key (sic_anatitem_zdb_id)
	);

      -- primary key
 
      create unique index stage_items_contained_primary_key_index 
	on stage_items_contained (sic_anatitem_zdb_id);

      -- foreign keys

      alter table stage_items_contained add constraint sic_anatitem_zdb_id_foreign_key
	foreign key (sic_anatitem_zdb_id)
	references term ;
  

      -- ---- STAGE_ITEM_CHILD_LIST ----

      -- this table is populated/repopulated with anatomy relationship pairs
      -- at ONE stage a time.
      -- It is used in the process of populating anatomy_display table.
  
	drop table if exists stage_item_child_list;

      create table stage_item_child_list
	(
	  stimchilis_item_zdb_id	text,
	  stimchilis_child_zdb_id	text,
	  stimchilis_child_name		text not null,
	  stimchilis_child_name_order	text not null
	);

      -- primary key

      create unique index stage_item_child_list_primary_key_index
        on stage_item_child_list (stimchilis_item_zdb_id,
				  stimchilis_child_zdb_id) ;

      alter table stage_item_child_list add constraint stage_item_child_list_primary_key
        primary key (stimchilis_item_zdb_id,
		     stimchilis_child_zdb_id);

      -- foreign keys
      create index stimchilis_item_zdb_id_index
        on stage_item_child_list (stimchilis_item_zdb_id);

      alter table stage_item_child_list add constraint stimchilis_item_zdb_id_foreign_key
        foreign key (stimchilis_item_zdb_id)
	references term;

      create index stimchilis_child_zdb_id_index
        on stage_item_child_list (stimchilis_child_zdb_id);

      alter table stage_item_child_list add constraint stimchilis_child_zdb_id_foreign_key
        foreign key (stimchilis_child_zdb_id)
	references term;




      -- =================    CREATE OUTPUT TABLES     =======================
      --use a new table to collect data in case of an error; drop the old table
      --and rename the new table before returning.

      -- ---- ANATOMY_DISPLAY ----

      -- This table stores every stage except the Unknown stage, and every 
      -- anatomy item that exists at this stage along with its row(seq_num)
      -- and column(indent) coordinates in a tree format display.  

	drop table if exists anatomy_display_new;

      create table anatomy_display_new
	(
	  anatdisp_stg_zdb_id		text,
	  anatdisp_seq_num		integer,
	  anatdisp_item_zdb_id		text not null,
	  anatdisp_item_name		text  not null,
	  anatdisp_indent		integer not null
	);

      -- =================   POPULATE TABLES   ===============================

      -- ---------------------------------------------------
      --    ALL_ANATOMY_STAGE
      -- ---------------------------------------------------

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
	
      -- -----------------------------------------------------------------------
      --    ANATOMY_DISPLAY_NEW
      -- -----------------------------------------------------------------------

      -- Anatomy_display has variables that are stage based, so use each
      -- stage_id to insert associated term.

      for stageId in
	select stg_zdb_id 
	  from stage
       loop
         
        -- Start the seqNum at zero and indent at one.
         seqNum = 0; 
	 indent = 1;

        -- populate stage_items_contained with anatomy term in this stage
	insert into stage_items_contained 
		select allanatstg_anat_item_zdb_id
		  from all_anatomy_stage
		 where allanatstg_stg_zdb_id = stageId;

        -- find out the highest level term in this stage, call
        -- populate_anat_display_stage_children to recursively retrieve children
	for anatomyId, anatomyName, stgHoursStart in
      	     select crt.sic_anatitem_zdb_id, term_name, stg_hours_start 
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
	    loop
      	    execute populate_anat_display_stage_children(
        		stageId, anatomyId, anatomyName, indent, seqNum) into seqNum;

        end loop;

        -- clean up the stage specific tables for the next iteration
        delete from stage_items_contained;
        delete from stage_item_child_list;

      end loop;



    -- -------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- -------------------------------------------------------------------------

      drop table stage_items_contained;
      drop table stage_item_child_list;
      drop table all_anatomy_stage;
      drop table anatomy_display;

      -- ---- ANATOMY_DISPLAY ----

      
     alter table anatomy_display_new rename to anatomy_display;

      -- primary key

      create unique index anatomy_display_primary_key_index
        on anatomy_display (anatdisp_stg_zdb_id,
			    anatdisp_seq_num);
      alter table anatomy_display add constraint anatomy_display_primary_key
        primary key (anatdisp_stg_zdb_id, anatdisp_seq_num);

      -- foreign keys

      create index anatdisp_stg_zdb_id_index
        on anatomy_display (anatdisp_stg_zdb_id);

      alter table anatomy_display add constraint anatdisp_stg_zdb_id_foreign_key
        foreign key (anatdisp_stg_zdb_id)
	references stage
	  on delete cascade;

       
      create index anatdisp_item_zdb_id_index
        on anatomy_display (anatdisp_item_zdb_id);

      alter table anatomy_display add constraint anatdisp_item_zdb_id_foreign_key
        foreign key (anatdisp_item_zdb_id)
	references term
	  on delete cascade;

      create index anatdisp_item_name_index
        on anatomy_display (anatdisp_item_name);

  --   RELEASE ZDB_FLAG

  execute release_zdb_flag('regen_anatomy') into flagStatus;
  if flagStatus <> 0 then
    return 1;
  end if;

  return 0;

end;

$log$ LANGUAGE plpgsql;
  
