


-- ---------------------------------------------------------------------
-- REGEN_TERM
-- ---------------------------------------------------------------------

create or replace function regen_term()
  returns int as $log$ 

  -- populates term fast search tables:
  --  term_display: term's position in a dag display of a certain stage
  --  all_term_contains: each and every ancestor and descendant
    
    declare nSynonyms int;
     nGenesForThisItem int;
     nGenesForChildItems int;
     nDistinctGenes int;
     nGenosForThisItem int;
     nGenosForChildItems int;
     nDistinctGenos int;


      -- ---- ALL_TERM_CONTAINS ----
  
      -- this table stores every term term with every ancestor 
      -- that has a contains relationship and the shortest distance 
      -- between each pair.  In this case, a contains relationship
      -- is being defined as dagedit_id's "is_a", "part_of", which
      -- leaves out "develops_from".  Unfortunately, the only place
      -- where that is defined is in this file, when we have a generic
      -- DAG, we will probably need relationship type groups so that
      -- nothing has to be hardcoded.

    begin
      drop table if exists all_term_contains_new;

      create table all_term_contains_new
        (
	  alltermcon_container_zdb_id		text,
	  alltermcon_contained_zdb_id		text,
	  alltermcon_min_contain_distance	int not null
        );


      -- =================   POPULATE TABLES   ===============================

      -- -----------------------------------------------------------------------
      --     ALL_TERM_CONTAINS_NEW
      -- -----------------------------------------------------------------------

  

      perform populate_all_term_contains();

      
    -- -------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- -------------------------------------------------------------------------

      drop table all_term_contains;
      alter table all_term_contains_new rename to all_term_contains;

      -- primary key

    
  -- re-create self-records in all_term_contains
  insert into all_term_contains
              select term_zdb_id,term_zdb_id,0
              from term;

  return 0;

end;

$log$ LANGUAGE plpgsql;

