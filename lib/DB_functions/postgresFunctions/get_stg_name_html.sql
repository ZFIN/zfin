create or replace function get_stg_name_html(
  stgZdbId        text,
  nameType  	  text default NULL)

  returns text as $stgNameHtml$
  
  -- Generates a string containing the stage name with embedded hot links to 
  -- the stage index page except the Unknown stage. 
  --
  -- INPUT:
  --       stage zdb id
  --       name type: default NULL / abbrev / long 
  --         if abbrev, the display name is the stg_abbrev column
  --         if long, the display name is the stg_name_long column, but
  --  the hyperlink is only on the standard name part. 
  --         if NULL or others, the display name is the stg_name column
  -- 
  -- RETURN:
  --       stage name string with embeded hyperlink 
  --       for the "Unknown" stage, return "Unknown" text.
  --       If a non-existent stage ZDB ID is passed in, return "INVALID STAGE"
  --

  declare stgName         stage.stg_name%TYPE;
   stgAbbrev       stage.stg_abbrev%TYPE;
   stgNameExt      stage.stg_name_ext%TYPE;

   col            int;
   stgUrl		text := '/zf_info/zfbook/stages/index.html';
   stgNameAnchor	text;
   stgNameHtml    text;

  begin

  select stg_name, stg_abbrev, stg_name_ext
    into stgName, stgAbbrev, stgNameExt
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
     stgNameHtml = 'INVALID STAGE';
  elsif (stgName = 'Unknown') then
     stgNameHtml = stgName;
  else
       col = 1;
      while (substring(stgName,col,1) <> ':'
	 AND substring(stgName,col,1) <> ' ' ) loop
         col = col + 1;
      end loop;

       stgNameAnchor = substring(stgName,1,(col-1));

      if (nameType = 'abbrev') then
         stgNameHtml = '<a href='' || stgUrl || '#' || stgNameAnchor || ''>' || stgAbbrev || '</a>';

      elsif (nameType = 'long') then 
	 stgNameHtml = '<a href='' || stgUrl || '#' || stgNameAnchor || ''>' || stgName || '</a>' || ' ' || stgNameExt;

      else	
	 stgNameHtml = '<a href='' || stgUrl || '#' || stgNameAnchor || ''>' || stgName || '</a>';
      end if  ;    

  end if;

  return stgNameHtml;

end
$stgNameHtml$ LANGUAGE plpgsql
