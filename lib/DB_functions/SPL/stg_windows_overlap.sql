create function
stg_windows_overlap(
  w1StartStageZdbId like stage.stg_zdb_id,
  w1EndStageZdbId   like stage.stg_zdb_id,
  w2StartStageZdbId like stage.stg_zdb_id,
  w2EndStageZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- Returns true if the first stage window overlaps in any way with the 
  -- second stage window.
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   For each window:
  --     The start of the start stage must be <= the start of the end stage.  
  --     The end of the end stage must be >= the end of the start stage. 

  define w1StartStart decimal(7,2);
  define w1StartEnd   decimal(7,2);
  define w1EndStart   decimal(7,2);
  define w1EndEnd     decimal(7,2);
  define w2StartStart decimal(7,2);
  define w2StartEnd   decimal(7,2);
  define w2EndStart   decimal(7,2);
  define w2EndEnd     decimal(7,2);
  define overlaps     boolean;

  select stg_hours_start, stg_hours_end
    into w1StartStart, w1StartEnd
    from stage
    where stg_zdb_id = w1StartStageZdbId;

  select stg_hours_start, stg_hours_end
    into w1EndStart, w1EndEnd
    from stage
    where stg_zdb_id = w1EndStageZdbId;

  select stg_hours_start, stg_hours_end
    into w2StartStart, w2StartEnd
    from stage
    where stg_zdb_id = w2StartStageZdbId;

  select stg_hours_start, stg_hours_end
    into w2EndStart, w2EndEnd
    from stage
    where stg_zdb_id = w2EndStageZdbId;

  -- Got all the data, now check that it is usable:

  -- If any of the start and end times obtained above are NULL then
  -- the ZDB ID used to get them was invalid.

  if (w1StartStart is null) then 
    raise exception -746, 0,			-- !!! ERORR EXIT
      'stg_windows_overlap: ' ||
      'Invalid window 1 start stage (' || w1StartStageZdbId || 
      ') passed in.';
  end if

  if (w1EndStart is null) then 
    raise exception -746, 0,			-- !!! ERROR EXIT
      'stg_windows_overlap: ' ||
      'Invalid window 1 end stage (' || w1EndStageZdbId || 
      ') passed in.';
  end if

  if (w2StartStart is null) then 
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: ' ||
      'Invalid window 2 start stage (' || w2StartStageZdbId || 
      ') passed in.';
  end if

  if (w2EndStart is null) then 
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: ' ||
      'Invalid window 2 end stage (' || w2EndStageZdbId || 
      ') passed in.';
  end if

  -- Check that the windows are properly formed.  
  -- For each window:
  --   The start of the start stage must be <= the start of the end stage.
  --   The end of the end stage must be >= the end of the start stage.

  if (w1StartStart > w1EndStart or
      w1EndEnd < w1StartEnd) then
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: Window 1 is inconsistent.';
  end if

  if (w2StartStart > w2EndStart or
      w2EndEnd < w2StartEnd) then
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: Window 2 is inconsistent.';
  end if


  -- Data looks good, do the check.

  let overlaps = 't';

  if (w1StartStart >= w2EndEnd or
      w1EndEnd   <= w2StartStart) then
    let overlaps = 'f';
  end if

  return overlaps;

end function;
