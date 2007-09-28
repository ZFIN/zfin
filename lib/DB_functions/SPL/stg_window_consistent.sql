create function
stg_window_consistent (
  startStgZdbId like stage.stg_zdb_id,
  endStgZdbId   like stage.stg_zdb_id )

  returning boolean;

  -- A pair of stages are consistent if:
  --  the same stages 
  -- OR
  --  The start of the end stage  > the start of the start stage.
  -- OR 
  --  The end of the end stage  > the end of the start stage.
  -- 
  -- We have got rid of superstages, but having an Unknown stage from 
  -- 0.00 ~ 730d. Unkown to Zygote is not valid, while Zygote to Unkown is.  
  --   
  -- Returns 
  --   true:       the given stages are consistent as a stage window.
  --   false:      the given stages are NOT consistent as a stage window.
  --   -746 error: One or both of the stage ZDB IDs was invalid.

  define startStart, startEnd   like stage.stg_hours_start;
  define endStart, endEnd       like stage.stg_hours_start;
  define consistent   boolean;

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

  -- verify consistency  
  let consistent = 'f';

  if (startStgZdbId = endStgZdbId) then
	let consistent = 't';
  -- we allow same start stage, e.g. unknown starts from 0.0, 
  -- same as 1-cell
  elif (endStart >= startStart OR endEnd > startEnd) then
        let consistent = 't';
  end if

  return consistent;

end function;
