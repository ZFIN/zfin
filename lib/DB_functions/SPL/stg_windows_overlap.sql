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
  --   Each window pass the stg_window_consistent check.  
 
  define w1StartStart like stage.stg_hours_start;
  define w1EndEnd     like stage.stg_hours_start;
  define w2StartStart like stage.stg_hours_start;
  define w2EndEnd     like stage.stg_hours_start;
  define overlaps     boolean;
  define consistent   boolean;

  -- Check that the windows are properly formed.  
  let consistent = stg_window_consistent(w1StartStageZdbId,w1EndStageZdbId);
  if (not consistent) then
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: Window 1 is inconsistent.';
  end if

  let consistent =stg_window_consistent(w2StartStageZdbId,w2EndStageZdbId);
  if (not consistent) then
    raise exception -746, 0, 			-- !!! ERROR EXIT
      'stg_windows_overlap: Window 2 is inconsistent.';
  end if

  select stg_hours_start
    into w1StartStart
    from stage
    where stg_zdb_id = w1StartStageZdbId;

  select stg_hours_end
    into w1EndEnd
    from stage
    where stg_zdb_id = w1EndStageZdbId;

  select stg_hours_start
    into w2StartStart
    from stage
    where stg_zdb_id = w2StartStageZdbId;

  select stg_hours_end
    into w2EndEnd
    from stage
    where stg_zdb_id = w2EndStageZdbId;

  -- Data looks good, do the check.

  let overlaps = 't';

  if (w1StartStart >= w2EndEnd or
      w1EndEnd   <= w2StartStart) then
    let overlaps = 'f';
  end if

  return overlaps;

end function;
