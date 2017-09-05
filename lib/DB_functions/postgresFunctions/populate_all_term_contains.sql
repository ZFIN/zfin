

-- ---------------------------------------------------------------------
-- POPULATE_ALL_TERM_CONTAINS
-- ---------------------------------------------------------------------

create or replace function populate_all_term_contains()
  returns int as $log$

  -- find the transitive closure of term contains, 
  -- keeping only the closest ancestor

  -- called from regen_term

  declare dist int := 1;
   delta int :=1 ;

  begin 

  -- the first level is a gimmie from term_relationship
  -- also _all_ child nodes are explicitly listed
  -- so we only need to find ancestors of these child nodes
  insert into all_term_contains_new (alltermcon_container_zdb_id, 
  					alltermcon_contained_zdb_id,
  					alltermcon_min_contain_distance)
    select termrel_term_1_zdb_id,
	   termrel_term_2_zdb_id, 
	   dist
    from term_relationship
    where termrel_type in ('is_a','part_of','part of');

  -- continue as long as progress is made 
  -- there may be more elegant ways to do this so please do tell. 
  while (delta  <  (select count(*) from all_term_contains_new) ) loop
     dist = dist + 1;
    -- set the baseline for determining is progress is made
    select count(*) 
      into delta 
      from all_term_contains_new; 
		
    -- try adding new ancestors 
    insert into all_term_contains_new
      select distinct a.termrel_term_1_zdb_id,     -- A.ancestor
		      b.alltermcon_containeD_zdb_id,  -- B.child
		      dist                             -- min depth
	from term_relationship a,            -- source of all ancestors
             all_term_contains_new b     -- source of all childs 

	where b.alltermcon_min_contain_distance = (dist - 1) 
	      -- limit the search to the previous level          
          and b.alltermcon_containeR_zdb_id = a.termrel_term_2_zdb_id
	      -- B.ancestor == A.child
	      -- checking for duplicates here is where the time gets absurd  
	      -- (2:30 vs 0:06), so 
	      --   "kill em all and let god sort them out later"
	  and a.termrel_type in ('is_a', 'part_of', 'part of')
	      -- all_term_contains doesn't want develops_from relationships,
	      -- and it's better to explicitly include rather than exclude,
	      -- since we want the behavior to stay the same the next time
	      -- a new type is added
	  ;

  end loop;

    
  -- split out the keepers in one step usings the dbs strength with set 
  -- operations instead of n-1 peicemeal steps 
 create temp table all_term_contains_new_tmp (alltermcon_container_zdb_id text,
 	     	   			      alltermcon_contained_zdb_id text,
					      alltermcon_min_contain_distance int)
;
 insert into all_term_contains_new_tmp (alltermcon_container_zdb_id,
 	     	   			      alltermcon_contained_zdb_id,
					      alltermcon_min_contain_distance )
  select alltermcon_container_zdb_id,
	 alltermcon_contained_zdb_id,
	 min(alltermcon_min_contain_distance) as alltermcon_min_contain_distance
    from all_term_contains_new
    group by alltermcon_container_zdb_id, alltermcon_contained_zdb_id
    ;

  -- move the keepers to where they will live
  delete from all_term_contains_new;
  insert into all_term_contains_new 
    select distinct * 
      from all_term_contains_new_tmp
      ;

  -- return the number of rows kept as an hint of correctness
  select count(*) 
    into delta 
    from all_term_contains_new; 

  return delta;				  

end ;

$log$ LANGUAGE plpgsql;
