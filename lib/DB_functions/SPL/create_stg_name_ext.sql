drop function create_stg_name_ext;

create function
create_stg_name_ext(
  stgHrsStart      like stage.stg_hours_start,
  stgHrsEnd        like stage.stg_hours_end,
  stgOtherFeatures like stage.stg_other_features)

  returning varchar(50);      -- must be enough to hold hours & other features

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

  define stgNameExt      varchar(50);

  let stgNameExt = "(";

  -- get stage start time
  if (stgHrsStart <= 168.00) then
    let stgNameExt = stgNameExt || stgHrsStart || "h-";
  else
    let stgNameExt = stgNameExt || (stgHrsStart / 24)::integer || "d-";
  end if

  -- get stage end time
  if (stgHrsEnd <= 168.00) then
    let stgNameExt = stgNameExt || stgHrsEnd || "h";
  else
    let stgNameExt = stgNameExt || (stgHrsEnd / 24)::integer || "d";
  end if

  -- get other features
  if (stgOtherFeatures is not NULL) then
    let stgNameExt = stgNameExt || ", " || stgOtherFeatures;
  end if

  let stgNameExt = stgNameExt || ")";

  return stgNameExt;

end function;

