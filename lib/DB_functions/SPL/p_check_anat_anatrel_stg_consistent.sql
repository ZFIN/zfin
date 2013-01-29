----------------------------------------------------------------
-- This procedure checks that every term relationship pair
-- involving the input term (Anatomy only) still follows the stage rule 
-- with the new stage range definition of that anatomy item
-- If the child's start stage is the "Unknown" stage, assign the 
-- parent's start hour for this check, if the child's end stage is the 
-- "Unknown" stage, assign the parent's end hour. In case the parent
-- has an Unknown start/end stage, it doesn't affect this check.
--
-- INPUT VARS:
--  		anatZdbId
--		startStgZdbId
--		endStgZdbId	
--
-- OUTPUT VARS:
--		None
-- EFFECTS:
--		call procedure p_check_anatrel_stg_consistent
-- RETURNS:
--		successful: nothing
--		fails: throws a -746 exception
-------------------------------------------------------------

create procedure p_check_anat_anatrel_stg_consistent (
		anatZdbId 	like term.term_zdb_id,
		startStgZdbId	like term_stage.ts_start_stg_zdb_id,
		endStgZdbId	like term_stage.ts_end_stg_zdb_id
	)

    define childAnatZdbId 	like term.term_zdb_id;
    define parentAnatZdbId 	like term.term_zdb_id;
    define dageditId		like term_relationship.termrel_type;
    define startHour		like stage.stg_hours_start;
    define endHour       	like stage.stg_hours_end;
    define parentStartHour	like stage.stg_hours_start;
    define parentEndHour    	like stage.stg_hours_end;
    define startStgName		like stage.stg_name;
    define endStgName    	like stage.stg_name;
	

    -- retrieve the start hour and end hour of the input anatomy item

    select stg_hours_start, stg_name
      into startHour, startStgName
      from stage 
     where stg_zdb_id = startStgZdbId;

    select stg_hours_end, stg_name
      into endHour, endStgName
      from stage 
     where stg_zdb_id = endStgZdbId;


    -- check relationships, call p_check_anatrel_stg_consistent

    -- as a parent
    foreach 
	select termrel_term_2_zdb_id, termrel_type
	  into childAnatZdbId, dageditId
	  from term_relationship
	 where termrel_term_1_zdb_id = anatZdbId

	execute procedure p_check_anatrel_stg_consistent
		(anatZdbId, childAnatZdbId, dageditId, startHour, endHour);

    end foreach

    -- as a child
    foreach 
	select termrel_term_1_zdb_id, term_type
	  into parentAnatZdbId, dageditId
	  from term_relationship
	 where termrel_term_2_zdb_id = anatZdbId

	select sstart.stg_hours_start, send.stg_hours_end
          into parentStartHour, parentEndHour
          from anatomy_item, stage sstart, stage send
         where term_stage = parentAnatZdbId
           and ts_start_stg_zdb_id = sstart.stg_zdb_id
           and ts_end_stg_zdb_id = send.stg_zdb_id;

	 -- if child's start stage is Unknown, set it to be the parent's start
 	 -- if child's end stage is Unknown, set it to the parent's end
  	if ( startStgName = "Unknown") then
		let startHour = parentStartHour;
  	end if
  	if ( endStgName = "Unknown") then
		let endHour = parentEndHour;
 	 end if

	execute procedure p_check_anatrel_stg_consistent
		(parentAnatZdbId, anatZdbId, dageditId, parentStartHour, parentEndHour, startHour, endHour);

    end foreach;

end procedure;
