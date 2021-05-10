create or replace function 
fimg_overlaps_stg_window (
  imgZdbId       image.img_zdb_id%TYPE,
  startStageZdbId stage.stg_zdb_id%TYPE,
  endStageZdbId   stage.stg_zdb_id%TYPE )

  returns boolean as $overlaps$

  -- Returns true if any of the stage windows defined for the image overlaps 
  -- in any way with the stage window passed to the funciton.  
  -- 
  -- A -746 error is returned if any of these conditions fails:
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.  
 

  declare imgStartStart   stage.stg_hours_start%TYPE;
   imgEndEnd       stage.stg_hours_start%TYPE;
   windowStartStart stage.stg_hours_start%TYPE;
   windowEndEnd     stage.stg_hours_start%TYPE;
   overlaps         boolean;
   consistent   	  boolean;

  begin
  -- Check that the window is properly formed.
  consistent = stg_window_consistent(startStageZdbId, endStageZdbId);

  if (not consistent) then
    raise exception 'img_overlaps: stage window is inconsistent.';
  end if;

  -- Get start and stop times for the stages that are passed in

  select stg_hours_start
    into windowStartStart
    from stage
    where stg_zdb_id = startStageZdbId;

  select stg_hours_end
    into windowEndEnd
    from stage
    where stg_zdb_id = endStageZdbId;

  -- Window we are checking for is consistent.
  -- Get fish_image_stage records until we find one that overlaps.

  overlaps = 'f';
  for imgStartStart, imgEndEnd in
    select startStg.stg_hours_start, endStg.stg_hours_end
      from image_stage, stage startStg, stage endStg
     where imgstg_fimg_zdb_id = fimgZdbId
       and imgstg_start_stg_zdb_id = startStg.stg_zdb_id
       and imgstg_end_stg_zdb_id = endStg.stg_zdb_id
    loop

	if (windowStartStart < fimgEndEnd and
       	    windowEndEnd > fimgStartStart) then
       	    overlaps = 't';
    	    exit;          -- !!!! EXIT LOOP EARLY
     	end if;
    end loop;
 
  return overlaps;

end 

$overlaps$ LANGUAGE plpgsql

