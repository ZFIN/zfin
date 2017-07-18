create or replace function
stg_window_consistent (
  startStgZdbId text,
  endStgZdbId   text)

  returns boolean as $true$

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

  declare startEnd stage.stg_hours_end%TYPE;
		startStart stage.stg_hours_start%TYPE;
  		endStart stage.stg_hours_end%TYPE; 
		endEnd stage.stg_hours_start%TYPE;
  		consistent boolean := 'f';

  -- Get start and stop times for the stages that are passed in
 begin
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
    raise exception 'stg_window_consitent: Invalid start stage';
  end if;

  if (endEnd is null) then
    raise exception 'stg_window_consistent: Invalid end stage';
  end if ;

  -- verify consistency  

  if (startStgZdbId = endStgZdbId) then
	consistent := 't';
  -- we allow same start stage, e.g. unknown starts from 0.0, 
  -- same as 1-cell
  elsif (endStart >= startStart OR endEnd > startEnd) then
        consistent := 't';
  else 
	consistent := 'f';
  end if;

  return consistent;

end

$true$ LANGUAGE plpgsql;
