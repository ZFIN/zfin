drop function anatitem_overlaps_stg_window;

create function 
anatitem_overlaps_stg_window (
  anatItemZdbId   like anatomy_item.anatitem_zdb_id,
  startStageZdbId like stage.stg_zdb_id,
  endStageZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- Returns true if the stage window the anatomy item exists in overlaps in
  -- any way with stage window passed in to the routine
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   The start of the start stage must be <= the start of the end stage.  
  --   The end of the end stage must be >= the end of the start stage. 

  define anatStart        decimal(7,2);
  define anatEnd          decimal(7,2);
  define windowStartStart decimal(7,2);
  define windowStartEnd   decimal(7,2);
  define windowEndStart   decimal(7,2);
  define windowEndEnd     decimal(7,2);
  define overlaps         boolean;

  -- set debug file to '/tmp/debug-anatitem_overlaps_stg_window';
  -- trace on;

  select startStg.stg_hours_start, endStg.stg_hours_end
    into anatStart, anatEnd
    from anatomy_item, stage startStg, stage endStg
    where anatitem_zdb_id = anatItemZdbId
      and startStg.stg_zdb_id = anatitem_start_stg_zdb_id
      and endStg.stg_zdb_id = anatitem_end_stg_zdb_id;

  select stg_hours_start, stg_hours_end
    into windowStartStart, windowStartEnd
    from stage
    where stg_zdb_id = startStageZdbId;

  select stg_hours_start, stg_hours_end
    into windowEndStart, windowEndEnd
    from stage
    where stg_zdb_id = endStageZdbId;


  -- Got all the data, now check that it is usable:

  -- If any of the start and end times obtained above are NULL then
  -- the ZDB ID used to get them was invalid.

  if (anatStart is null) then
    raise exception -746, 0,			-- !!! ERROR EXIT
      'anatitem_overlaps: ' ||
      'Start stage start hours is null (' || anatItemZdbId || ')';
  end if

  if (windowStartStart is null) then
    raise exception -746, 0,			-- !!! ERROR EXIT
      'anatitem_overlaps: ' ||
      'Invalid start stg (' || startStageZdbId || ')';
  end if

  if (windowEndEnd is null) then
    raise exception -746, 0,			-- !!! ERROR EXIT
      'anatitem_overlaps: ' ||
      'Invalid end stage (' || endStageZdbId || ')';
  end if

  -- The start of the start stage must be <= the start of the end stage.
  -- The end of the end stage must be >= the end of the start stage.

  if (windowStartStart > windowEndStart) then
    raise exception -746, 0,			-- !!! ERROR EXIT
      'anatitem_overlaps: Start of start stage must be <= start of end stage';
  end if

  if (windowEndEnd < windowStartEnd) then
    raise exception -746, 0, 
      'anatitem_overlaps: End of end stage must be >= end of start stage.';
  end if

  -- we assume the stage window in anatomy item is valid.

  -- all the data is usable. use it.

  let overlaps = 't';

  if (anatStart >= windowEndEnd or
      anatEnd   <= windowStartStart) then
    let overlaps = 'f';
  end if

  return overlaps;

end function;
