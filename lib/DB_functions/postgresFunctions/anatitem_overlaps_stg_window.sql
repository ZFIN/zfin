create or replace function anatitem_overlaps_stg_window (anatItemZdbId varchar, startStageZdbId varchar, endStageZdbId varchar) returns boolean as $$ 

  -- Returns true if the stage window the anatomy item exists in overlaps in
  -- any way with stage window passed in to the routine
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.

  declare anatStart        stage.stg_hours_start%TYPE;
   	  anatEnd          stage.stg_hours_start%TYPE;
   	  windowStartStart stage.stg_hours_start%TYPE;
   	  windowEndEnd     stage.stg_hours_start%TYPE;
   	  overlaper        boolean;
   	  consistent   	   boolean := stg_window_consistent(startStageZdbId, endStageZdbId);
  begin
  if (not consistent) then
    raise exception 'anatitem_overlaps: stage window is inconsistent.',startStageZdbId, endStageZdbId
       using hint = 'make stage window consistent'		-- !!! ERROR EXIT 
  end if;
	select startStg.stg_hours_start, endStg.stg_hours_end
    	   into anatStart, anatEnd
    	   from term_stage, stage startStg, stage endStg
   	        where ts_term_zdb_id = anatItemZdbId
      		and startStg.stg_zdb_id = anatitem_start_stg_zdb_id
      		and endStg.stg_zdb_id = anatitem_end_stg_zdb_id;

  	select stg_hours_start
    	       into windowStartStart
    	       from stage
   	       	     where stg_zdb_id = startStageZdbId;

  	select stg_hours_end
    	       into windowEndEnd
    	       from stage
    	       	    where stg_zdb_id = endStageZdbId;

  	let overlaper = 't';

  	if (anatStart >= windowEndEnd or
      	   anatEnd   <= windowStartStart) then
    	   let overlaper = 'f';
  	end if;
 
	return overlaper;

  end

$$ LANGUAGE plpgsql;

