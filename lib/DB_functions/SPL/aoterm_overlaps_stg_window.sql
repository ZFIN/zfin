create function 
aoterm_overlaps_stg_window (
  anatItemZdbId   like term.term_zdb_id,
  startStageZdbId like stage.stg_zdb_id,
  endStageZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- Returns true if the stage window the anatomy item exists in overlaps in
  -- any way with stage window passed in to the routine
  -- this function mirrors anatitem_overlaps_stg_window with a term ID rather than a anatomy ID
  -- A -746 error is returned if any of these conditions fails.
  --   All parameters must be non-null.  
  --   Each window pass the stg_window_consistent check.

  define anatStart        like stage.stg_hours_start;
  define anatEnd          like stage.stg_hours_start;
  define windowStartStart like stage.stg_hours_start;
  define windowEndEnd     like stage.stg_hours_start;
  define overlaps         boolean;
  define consistent   	  boolean;

  --  Check that the window is properly formed.
  let consistent = stg_window_consistent(startStageZdbId, endStageZdbId);
  if (not consistent) then
    raise exception -746, 0,			-- !!! ERROR EXIT
      'aoterm_overlaps: stage window is inconsistent.';
  end if

  select startStg.stg_hours_start, endStg.stg_hours_end
    into anatStart, anatEnd
    from anatomy_item, stage startStg, stage endStg, term
    where term_zdb_id = anatItemZdbId
      and anatitem_obo_id = term_ont_id
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

  let overlaps = 't';

  if (anatStart >= windowEndEnd or
      anatEnd   <= windowStartStart) then
    let overlaps = 'f';
  end if

  return overlaps;

end function;
