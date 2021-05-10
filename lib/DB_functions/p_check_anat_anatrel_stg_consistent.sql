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

create or replace function p_check_anat_anatrel_stg_consistent (
		anatZdbId 	text,
		startStgZdbId	text,
		endStgZdbId	text
	)
returns void as $$

    declare childAnatZdbId 	term.term_zdb_id%TYPE;
     parentAnatZdbId 	term.term_zdb_id%TYPE;
     dageditId		term_relationship.termrel_type%TYPE;
     startHour		stage.stg_hours_start%TYPE;
     endHour       	stage.stg_hours_end%TYPE;
     parentStartHour	stage.stg_hours_start%TYPE;
     parentEndHour    	stage.stg_hours_end%TYPE;
     startStgName	stage.stg_name%TYPE;
     endStgName    	stage.stg_name%TYPE;
	
    begin 
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
    for childAnatZdbId, dageditId in 
	select termrel_term_2_zdb_id, termrel_type
	  from term_relationship
	 where termrel_term_1_zdb_id = anatZdbId

	loop

	select p_check_anatrel_stg_consistent
		(anatZdbId, childAnatZdbId, dageditId, startHour, endHour);

    end loop;

    -- as a child
    for parentAnatZdbId, dageditId in
	select termrel_term_1_zdb_id, term_type
	  from term_relationship
	 where termrel_term_2_zdb_id = anatZdbId

	loop

	select sstart.stg_hours_start, send.stg_hours_end
          into parentStartHour, parentEndHour
          from anatomy_item, stage sstart, stage send
         where term_stage = parentAnatZdbId
           and ts_start_stg_zdb_id = sstart.stg_zdb_id
           and ts_end_stg_zdb_id = send.stg_zdb_id;

	 -- if child's start stage is Unknown, set it to be the parent's start
 	 -- if child's end stage is Unknown, set it to the parent's end
  	if ( startStgName = 'Unknown') then
		startHour = parentStartHour;
  	end if;
  	if ( endStgName = "Unknown") then
		endHour = parentEndHour;
 	 end if;

	select p_check_anatrel_stg_consistent
		(parentAnatZdbId, anatZdbId, dageditId, parentStartHour, parentEndHour, startHour, endHour);

    end loop;

end

$$ LANGUAGE plpgsql
