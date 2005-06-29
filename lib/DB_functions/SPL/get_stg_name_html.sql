create function
get_stg_name_html(
  stgZdbId        like stage.stg_zdb_id,
  nameType  	  varchar(30) default NULL)

  returning varchar(200);

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

  define stgName        like stage.stg_name;
  define stgAbbrev      like stage.stg_abbrev;
  define stgNameExt     like stage.stg_name_ext;

  define col            int;
  define stgUrl		varchar(60);
  define stgNameAnchor	varchar(60);
  define stgNameHtml    varchar(200);
  let stgUrl = "/zf_info/zfbook/stages/index.html";

  select stg_name, stg_abbrev, stg_name_ext
    into stgName, stgAbbrev, stgNameExt
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
    let stgNameHtml = "INVALID STAGE";
  elif (stgName = "Unknown") then
    let stgNameHtml = stgName;
  else
      let col  = 1;
      while (substring(stgName from col for 1) <> ":"
	 AND substring(stgName from col for 1) <> " " )
        let col = col + 1;
      end while

      let stgNameAnchor = substring(stgName from 1 for col - 1);

      if (nameType = "abbrev") then
        let stgNameHtml = '<a href="' || stgUrl || "#" || stgNameAnchor || '">' || stgAbbrev || "</a>";

      elif (nameType = "long") then 
	let stgNameHtml = '<a href="' || stgUrl || "#" || stgNameAnchor || '">' || stgName || "</a>" || " " || stgNameExt;

      else	
	let stgNameHtml = '<a href="' || stgUrl || "#" || stgNameAnchor || '">' || stgName || "</a>";
      end if      

  end if

  return stgNameHtml;

end function;
