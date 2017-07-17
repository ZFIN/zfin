create or replace function create_stg_name_ext(
  stgHrsStart      decimal(7,2),
  stgHrsEnd        decimal(7,2),
  stgOtherFeatures text)

  returns varchar(50) as $stgNameExt$

  -- Creates the extension part of the "long name" for a stage, i.e, the part
  -- that makes a long name long.  The extension includes the hours the 
  -- stage spans, and the contents of the stg_other_features column.
  -- In addition, any times greater than 168 hours (7 days) are displayed
  -- as days, rather than as hours.
  --
  -- Examples
  --
  --   (60.00h-72.00h)
  --   (60.00h-72.00h, 3.0mm SL)
  --   (60.00h-72.00h, 3.0mm NL)
  --   (60.00h-72.00h, 3.0/4.0mm NL/SL)
  --   (168.00h-90d)
  --   (90d-730d)
  --

  declare stgNameExt      varchar(50) := '(';
  begin
  -- get stage start time
  if (stgHrsStart <= 168.00) then
     stgNameExt = stgNameExt || stgHrsStart || 'h-';
  else
     stgNameExt = stgNameExt || (stgHrsStart / 24)::integer || 'd-';
  end if;

  -- get stage end time
  if (stgHrsEnd <= 168.00) then
     stgNameExt = stgNameExt || stgHrsEnd || 'h';
  else
     stgNameExt = stgNameExt || (stgHrsEnd / 24)::integer || 'd';
  end if;

  -- get other features
  if (stgOtherFeatures is not NULL) then
     stgNameExt = stgNameExt || ', ' || stgOtherFeatures;
  end if;

   stgNameExt = stgNameExt || ')';

  return stgNameExt;

end 

$stgNameExt$ LANGUAGE plpgsql
