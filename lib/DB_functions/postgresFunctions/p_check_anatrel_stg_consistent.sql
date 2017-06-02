-------------------------------------------------------------------------      
-- The procedure checks that in the case of "is_a" or "part_of"
-- relationship, the child anatomy item must have a stage range 
-- within the parent's, and in the case of "develops_from"
-- relationship, the child item must start no earlier than the parent's
-- start time and no later than the following stage of the parent's end 
-- stage. If the child's start stage is the "Unknown" stage, assign the 
-- parent's start hour for this check, if the child's end stage is the 
-- "Unknown" stage, assign the parent's end hour. In case the parent
-- has an Unknown start/end stage, it doesn't affect this check. 
--
-- INPUT VARS:
--           parent anatomy item zdb id
--           child anatomy item zdb id
--           relationship dagedit id
--  optional:
--           parent start hour
--	     parent end hour
--           child start hour
--           child end hour
-- In case of an update action on an anatomy item, the new stage need to 
-- be provided. In other cases, query out the current stages.
--
-- OUTPUT VARS:
--           None
--
-- EFFECTS:
--           None
--
-- RETURNS:
-- 	     Check successful: nothing
--           Check fails : throws a -746 exception
--	    
-- This procedure is used by anatomy_relationship insert and update trigger
-- to ensure relationship assignment. It is also used by anatomy_item 
-- update trigger to ensure the change on item stage range doesn't cause 
-- any of its existing anatomy relationship to be violating the rule.  
-------------------------------------------------------------------------


create or replace function  p_check_anatrel_stg_consistent (
		parentAnatZdbId    varchar(50) default NULL,
	   	childAnatZdbId	   varchar(50) default NULL,
		relDageditId	   varchar(50) default NULL,	
		parentStartHour	   varchar(50) default NULL,
		parentEndHour	   varchar(50) default NULL,
		childStartHour	   varchar(50) default NULL,
		childEndHour	   varchar(50) default NULL,
		childOboID	   varchar(50) default NULL,
		parentOboID	   varchar(50) default NULL
	
	)
returns void as $$

      declare childStartStgName	stage.stg_name%TYPE :='';
       childEndStgName    stage.stg_name%TYPE := '';
begin
  -- in case of default value for stage hour, query db for the 
  -- current value.

  if ( parentStartHour is NULL ) then

      select stg_hours_start
        into parentStartHour
        from term_stage, stage 
       where ts_term_zdb_id = parentAnatZdbId
        and ts_start_stg_zdb_id = stg_zdb_id;
  end if; 

  if ( parentEndHour is NULL ) then

      select stg_hours_end
        into parentEndHour
        from term_stage, stage
       where ts_term_zdb_id = parentAnatZdbId
        and ts_end_stg_zdb_id = stg_zdb_id;
  end if ;


  if ( childStartHour is NULL ) then

      select stg_hours_start, stg_name
        into childStartHour, childStartStgName
        from term_stage, stage 
       where ts_term_zdb_id = childAnatZdbId
         and ts_start_stg_zdb_id = stg_zdb_id;
  end if ;

  if ( childEndHour is NULL ) then

      select stg_hours_end, stg_name
        into childEndHour, childEndStgName
        from term_stage, stage
       where ts_term_zdb_id = childAnatZdbId
         and ts_end_stg_zdb_id = stg_zdb_id;
  end if ;

  -- if child's start stage is Unknown, set it to be the parent's start
  -- if child's end stage is Unknown, set it to the parent's end
  -- if parent's start stage or end stage is Unknow, that won't break 
  -- this consistency check
  if (childStartStgName = 'Unknown') then
	childStartHour = parentStartHour;
  end if;
  if (childEndStgName = 'Unknown') then
	childEndHour = parentEndHour;
  end if;

  select term_ont_id
  into childOboID
  from term
  where term_zdb_id = childAnatZdbId;

  select term_ont_id
  into parentOboID
  from term
  where term_zdb_id = parentAnatZdbId;

  -- For is_a and part_of relationship, child term must has a stage range
  -- within the parent term. If violate, raise -746 error. 

  if (relDageditId = 'is_a' OR relDageditId = 'part_of') then	
    if (childStartHour < parentStartHour OR childEndHour > parentEndHour) then
	  raise exception 'hours do nott match stage errors';
    end if ;
  end if ;


  -- For develops_from relationship, child term must has a start stage 
  -- that is no earlier than parent's start, and no later than the following
  -- stage of parent"s end. If violate, raise -746 error. 

  if (relDageditId = 'develops_from') then	
    if (childStartHour < parentStartHour OR childStartHour > parentEndHour) then
	  raise exception 'child must start no earlier than parent, no later than the following stage of parent end stage';
    end if ;
  end if ;
end

$$ LANGUAGE plpgsql
  
