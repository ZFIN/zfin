drop function fimg_overlaps_stg_window;

create function 
fimg_overlaps_stg_window (
  fimgZdbId       like fish_image.fimg_zdb_id,
  startStageZdbId like stage.stg_zdb_id,
  endStageZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- Returns true if any of the stage windows defined for the image overlaps 
  -- in any way with the stage window passed to the funciton.  
  -- 
  -- A -746 error is returned if any of these conditions fails:
  --   All parameters must be non-null.  
  --   The start of the start stage must be <= the start of the end stage.  
  --   The end of the end stage must be >= the end of the start stage. 


  define fimgStartStart   decimal(7,2);
  define fimgStartEnd     decimal(7,2);
  define fimgEndStart     decimal(7,2);
  define fimgEndEnd       decimal(7,2);
  define windowStartStart decimal(7,2);
  define windowStartEnd   decimal(7,2);
  define windowEndStart   decimal(7,2);
  define windowEndEnd     decimal(7,2);
  define overlaps         boolean;

  -- Get start and stop times for the stages that are passed in

  select stg_hours_start, stg_hours_end
    into windowStartStart, windowStartEnd
    from stage
    where stg_zdb_id = startStageZdbId;

  select stg_hours_start, stg_hours_end
    into windowEndStart, windowEndEnd
    from stage
    where stg_zdb_id = startStageZdbId;

  -- verify that we actually got start and stop times.

  if (windowStartStart is null) then
    raise exception -746, 0,		-- !!! ERROR EXIT
      'fimg_overlaps: ' ||
      'Invalid start stage (' || startStageZdbId || ')';
  end if

  if (windowEndEnd is null) then
    raise exception -746, 0,		-- !!! ERROR EXIT
      'fimg_overlaps: ' ||
      'Invalid end stage (' || endStageZdbId || ')';
  end if

  -- The start of the start stage must be <= the start of the end stage.
  -- The end of the end stage must be >= the end of the start stage.

  if (windowStartStart > windowEndStart) then
    raise exception -746, 0,		 -- !!! ERROR EXIT
      'fimg_overlaps: Start of start stage must be <= start of end stage';
  end if

  if (windowEndEnd < windowStartEnd) then
    raise exception -746, 0,		-- !!! ERROR EXIT
      'fimg_overlaps: End of end stage must be >= end of start stage.';
  end if


  -- Window we are checking for is consistent.
  -- Get fish_image_stage records until we find one that overlaps.

  let overlaps = 'f';

  foreach 
    select startStg.stg_hours_start, startStg.stg_hours_end,
	   endStg.stg_hours_start, endStg.stg_hours_end
      into fimgStartStart, fimgStartEnd, fimgEndStart, fimgEndEnd
      from fish_image_stage, stage startStg, stage endStg
      where fimgstg_fimg_zdb_id = fimgZdbId
        and fimgstg_start_stg_zdb_id = startStg.stg_zdb_id
	and fimgstg_end_stg_zdb_id = endStg.stg_zdb_id

    if (windowStartStart < fimgEndEnd and
        windowEndEnd > fimgStartStart) then
      let overlaps = 't';
      exit foreach;		-- !!!! EXIT LOOP EARLY
    end if
  end foreach

  return overlaps;

end function;
