create function 
fimg_overlaps_stg_window (
  imgZdbId       like image.img_zdb_id,
  startStageZdbId like stage.stg_zdb_id,
  endStageZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- Returns true if any of the stage windows defined for the image overlaps 
  -- in any way with the stage window passed to the funciton.  
  -- 
  -- A -746 error is returned if any of these conditions fails:
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.  
 

  define imgStartStart   like stage.stg_hours_start;
  define imgEndEnd       like stage.stg_hours_start;
  define windowStartStart like stage.stg_hours_start;
  define windowEndEnd     like stage.stg_hours_start;
  define overlaps         boolean;
  define consistent   	  boolean;

  -- Check that the window is properly formed.
  let consistent = stg_window_consistent(startStageZdbId, endStageZdbId);
  if (not consistent) then
    raise exception -746, 0,		 -- !!! ERROR EXIT
      'img_overlaps: stage window is inconsistent.';
  end if

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

  let overlaps = 'f';
  foreach 
    select startStg.stg_hours_start, endStg.stg_hours_end
      into imgStartStart, imgEndEnd
      from image_stage, stage startStg, stage endStg
     where imgstg_fimg_zdb_id = fimgZdbId
       and imgstg_start_stg_zdb_id = startStg.stg_zdb_id
       and imgstg_end_stg_zdb_id = endStg.stg_zdb_id

    if (windowStartStart < fimgEndEnd and
        windowEndEnd > fimgStartStart) then
       let overlaps = 't';
       exit foreach;          -- !!!! EXIT LOOP EARLY
     end if
  end foreach 
  return overlaps;

end function;
