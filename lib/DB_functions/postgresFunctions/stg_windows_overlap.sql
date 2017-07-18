create or replace function stg_windows_overlap(
  w1StartStageZdbId text),
  w1EndStageZdbId text,
  w2StartStageZdbId text,
  w2EndStageZdbId text)

  returns boolean as $true$

  -- Returns true if the first stage window overlaps in any way with the 
  -- second stage window.
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.  
 
  declare w1StartStart  stage.stg_hours_start%TYPE;
   w1EndEnd      stage.stg_hours_start%TYPE;
   w2StartStart  stage.stg_hours_start%TYPE;
   w2EndEnd      stage.stg_hours_start%TYPE;
   overlaps     boolean;
   consistent   boolean;

 begin 
  -- Check that the windows are properly formed.  
  consistent := stg_window_consistent(w1StartStageZdbId,w1EndStageZdbId);
  if (not consistent) then
    raise exception 'stg_windows_overlap: Window 1 is inconsistent.';
  end if;

  consistent := stg_window_consistent(w2StartStageZdbId,w2EndStageZdbId);
  if (not consistent) then
    raise exception 'stg_windows_overlap: Window 2 is inconsistent.';
  end if;

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

  overlaps := 't';

  if (w1StartStart >= w2EndEnd or
      w1EndEnd   <= w2StartStart) then
    overlaps := 'f';
  end if;

  return overlaps;

end
$true$ LANGUAGE plpgsql;

