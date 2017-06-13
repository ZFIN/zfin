create or replace function 
aoterm_overlaps_stg_window (
  anatItemZdbId   varchar(50),
  startStageZdbId varchar(50),
  endStageZdbId   varchar(50))

  returns boolean as $true$

  -- Returns true if the stage window the anatomy item exists in overlaps in
  -- any way with stage window passed in to the routine
  -- this function mirrors anatitem_overlaps_stg_window with a term ID rather than a anatomy ID
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.

  declare anatStart         stage.stg_hours_start%TYPE;
   anatEnd           stage.stg_hours_start%TYPE;
   windowStartStart  stage.stg_hours_start%TYPE;
   windowEndEnd      stage.stg_hours_start%TYPE;
   overlaps         boolean;
   consistent   	  boolean := stg_window_consistent(startStageZdbId, endStageZdbId);

 begin
  if (not consistent) then
    raise exception 'aoterm_overlaps: stage window is inconsistent.';
  end if;

  select startStg.stg_hours_start, endStg.stg_hours_end
    into anatStart, anatEnd
    from term_stage, stage startStg, stage endStg
    where anatItemZdbId = ts_term_zdb_id
      and startStg.stg_zdb_id = ts_start_stg_zdb_id
      and endStg.stg_zdb_id = ts_end_stg_zdb_id;

  select stg_hours_start
    into windowStartStart
    from stage
    where stg_zdb_id = startStageZdbId;

  select stg_hours_end
    into windowEndEnd
    from stage
    where stg_zdb_id = endStageZdbId;

  overlaps = 't';

  if (anatStart >= windowEndEnd or
      anatEnd   <= windowStartStart) then
     overlaps = 'f';
  end if;

  return overlaps;

end
$true$ LANGUAGE plpgsql
