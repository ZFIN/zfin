create function
create_stg_name_long(
  stgName          like stage.stg_name,
  stgHrsStart      like stage.stg_hours_start,
  stgHrsEnd        like stage.stg_hours_end,
  stgOtherFeatures like stage.stg_other_features)

  returning varchar(130);

  -- Creates the "long name" for a stage.  The long name includes
  -- the name of the stage, plus the hours it spans, and the contents 
  -- of the stg_other_features column.
  -- In addition, any times greater than 168 hours (7 days) are displayed
  -- as days, rather than as hours.
  --
  -- If a non-existent stage ZDB ID is passed in then
  --
  --   UNKNOWN
  --
  -- is returned.
  -- 
  -- Examples
  --
  --   Large-stage:Sub-stage (60.00h - 72.00h)
  --   Large-stage:Sub-stage (60.00h - 72.00h, 3.0mm SL)
  --   Large-stage:Sub-stage (60.00h - 72.00h, 3.0mm NL)
  --   Large-stage:Sub-stage (60.00h - 72.00h, 3.0/4.0mm NL/SL)
  --   Larval:Middle to late (168.00h - 90d)
  --   Adult (90 d to 730 d)
  --

  define stgNameLong      varchar(130);

  if (stgName is NULL) then
    let stgNameLong = "UNKNOWN";
  else
    -- get stage name 
    let stgNameLong = stgName || " " ||
		      create_stg_name_ext(stgHrsStart, stgHrsEnd,
					  stgOtherFeatures);
  end if;

  return stgNameLong;

end function;
