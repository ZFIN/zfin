drop function get_stg_name_long_html;

create function
get_stg_name_long_html(
  stgZdbId        like stage.stg_zdb_id,
  javascriptFunc  varchar(80) default NULL)

  returning varchar(255);

  -- Creates the "long name" for a stage with embedded HREF tags in it.
  -- The long name includes the name of the stage, plus the hours it spans, 
  -- and the contents of the stg_other_features column.
  -- 
  -- If the OPTIONAL javascriptFunc parameter is provided then the generated
  -- HTML will invoke that javascript routine, passing the URL in 
  -- stg_comments_relative_url as a parameter to the javascript function.
  --
  -- If a non-existent stage ZDB ID is passed in then
  --
  --   UNKNOWN
  --
  -- is returned.
  --
  -- Examples
  --
  --   Large-stage : Sub-stage (60.00h - 72.00h)
  --   Large-stage : Sub-stage (60.00h - 72.00h, 3.0mm SL)
  --   Large-stage : Sub-stage (60.00h - 72.00h, 3.0mm NL)
  --   Large-stage : Sub-stage (60.00h - 72.00h, 3.0/4.0mm NL/SL)
  --   Larval : Middle to late (168.00h - 90d)
  --   Adult (90 d to 730 d)
  --

  define stgNameLongHtml	varchar(255);
  define stgName		like stage.stg_name;
  define stgNameExt		like stage.stg_name_ext;

  select stg_name, stg_name_ext
    into stgName, stgNameExt
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
    let stgNameLongHtml = "UNKNOWN";
  else
    let stgNameLongHtml = get_stg_name_html(stgZdbId, javascriptFunc) || 
			  " " || stgNameExt;
  end if

  return stgNameLongHtml;

end function;
