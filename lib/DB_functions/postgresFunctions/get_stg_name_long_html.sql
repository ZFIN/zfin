create or replace function
get_stg_name_long_html(
  stgZdbId        varchar(50),
  javascriptFunc  varchar(80) default NULL)

  returns varchar(255) as $stgNameLongHtml$

  -- Creates the "long name" for a stage with embedded HREF tags in it.
  -- The long name includes the name of the stage, plus the hours it spans, 
  -- and the contents of the stg_other_features column. The Unknown stage 
  -- has the same long name as the name. 
  -- 
  -- If the OPTIONAL javascriptFunc parameter is provided then the generated
  -- HTML will invoke that javascript routine, passing the URL of the stage 
  -- index page with proper anchor as a parameter to the javascript function.
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
  --   Unknown

  declare stgNameLongHtml	varchar(255);
   stgName		 stage.stg_name%TYPE;
   stgNameExt		 stage.stg_name_ext%TYPE;

 begin 

  select stg_name, stg_name_ext
    into stgName, stgNameExt
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
     stgNameLongHtml = "UNKNOWN";
  elsif (stgName = "Unknown") then
     stgNameLongHtml = "Unknown";
  else
     stgNameLongHtml = get_stg_name_html(stgZdbId, javascriptFunc) || 
			  ' ' || stgNameExt;
  end if;

  return stgNameLongHtml;
end
$stgNameLongHtml$ LANGUAGE plpgsql
