drop function stg_window_consistent;

create function
stg_window_consistent (
  startStgZdbId like stage.stg_zdb_id,
  endStgZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- A pair of stages are consistent if:
  --   The start of the start stage must be <= the start of the end stage.  
  --   The end of the end stage must be >= the end of the start stage. 
  --   
  -- Returns 
  --   true:       the given stages are consistent as a stage window.
  --   false:      the given stages are NOT consistent as a stage window.
  --   -746 error: One or both of the stage ZDB IDs was invalid.

  define startStart   decimal(7,2);
  define startEnd     decimal(7,2);
  define endStart     decimal(7,2);
  define endEnd       decimal(7,2);
  define consistent   boolean;

  -- set debug file to '/tmp/debug-stg_window_consistent';
  -- trace on;

  -- Get start and stop times for the stages that are passed in

  select stg_hours_start, stg_hours_end
    into startStart, startEnd
    from stage
    where stg_zdb_id = startStgZdbId;

  select stg_hours_start, stg_hours_end
    into endStart, endEnd
    from stage
    where stg_zdb_id = endStgZdbId;

  -- verify that we actually got start and stop times.

  if (startStart is null) then
    raise exception -746, 0,		-- !!! ERROR EXIT
      'stg_window_consitent: ' ||
      'Invalid start stage (' || startStgZdbId || ')';
  end if

  if (endEnd is null) then
    raise exception -746, 0,		-- !!!! ERROR EXIT
      'stg_window_consistent: ' ||
      'Invalid end stage (' || endStgZdbId || ')';
  end if

  -- The start of the start stage must be <= the start of the end stage.
  -- The end of the end stage must be >= the end of the start stage.

  let consistent = 't';

  if (startStart > endStart or
      endEnd < startEnd) then
    let consistent = 'f';
  end if

  return consistent;

end function;

update statistics for function stg_window_consistent;