-------------------------------------------------------------------------      
-- The procedure checks that in the case of "is_a" or "part_of"
-- relationship, the child anatomy item must have a stage range 
-- within the parent's, and in the case of "develops_from"
-- relationship, the child item must start no earlier than the parent's
-- start time and no later than the following stage of the parent's end 
-- stage. 
--
-- INPUT VARS:
--           parent anatomy item zdb id
--           child anatomy item zdb id
--           relationship dagedit id
--  optional:
--           parent start hour
--           parent end hour
--           child start hour
--           child end hour
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
-- When optional parameters are not provided, the current value for that
-- anatomy item is used.
-- This procedure is used by anatomy_relationship insert and update trigger
-- to ensure relationship assignment. It is also used by anatomy_item 
-- update trigger to ensure the change on item stage range doesn't cause 
-- any of its existing anatomy relationship to be violating the rule.  
-------------------------------------------------------------------------


create procedure p_check_anatrel_stg_consistent (
		parentAnatZdbId    like anatomy_item.anatitem_zdb_id,
	   	childAnatZdbId	   like anatomy_item.anatitem_zdb_id,
		relDageditId	   like anatomy_relationship_type.areltype_dagedit_id,	
		parentStartHour	   like stage.stg_hours_start default NULL,
		parentEndHour      like stage.stg_hours_end default NULL,
		childStartHour	   like stage.stg_hours_start default NULL,
		childEndHour       like stage.stg_hours_end default NULL
	)
  
  -- in case of default value for stage hour, query db for the 
  -- current value.

  if ( parentStartHour is NULL ) then

      select stg_hours_start
        into parentStartHour
        from anatomy_item, stage 
       where anatitem_zdb_id = parentAnatZdbId
         and anatitem_start_stg_zdb_id = stg_zdb_id;
  end if 

  if ( parentEndHour is NULL ) then

      select stg_hours_end
        into parentEndHour
        from anatomy_item, stage
       where anatitem_zdb_id = parentAnatZdbId
         and anatitem_end_stg_zdb_id = stg_zdb_id;
  end if 


  if ( childStartHour is NULL ) then

      select stg_hours_start
        into childStartHour
        from anatomy_item, stage 
       where anatitem_zdb_id = childAnatZdbId
         and anatitem_start_stg_zdb_id = stg_zdb_id;
  end if 

  if ( childEndHour is NULL ) then

      select stg_hours_end
        into childEndHour
        from anatomy_item, stage
       where anatitem_zdb_id = childAnatZdbId
         and anatitem_end_stg_zdb_id = stg_zdb_id;
  end if 


  -- For is_a and part_of relationship, child term must has a stage range
  -- within the parent term. If violate, raise -746 error. 

  if (relDageditId = "is_a" OR relDageditId = "part_of") then	
    if (childStartHour < parentStartHour OR childEndHour > parentEndHour) then
	  raise exception -746, 0, 
	    "For " || relDageditId || " relationship, "||
	    "child stage range must be within parent stage range";
    end if 
  end if 


  -- For develops_from relationship, child term must has a start stage 
  -- that is no earlier than parent's start, and no later than the following
  -- stage of parent"s end. If violate, raise -746 error. 

  if (relDageditId = "develops_from") then	
    if (childStartHour < parentStartHour OR childStartHour > parentEndHour) then
	  raise exception -746, 0, 
	    "For " || relDageditId || ", child must start no earlier than parent, "||
            "no later than the following stage of parent end stage";
    end if 
  end if 

end procedure;

  