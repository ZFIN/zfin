drop function get_stg_name_html;

create function
get_stg_name_html(
  stgZdbId        like stage.stg_zdb_id,
  javascriptFunc  varchar(80) default NULL)

  returning varchar(200);

  -- Generates a string containing the stage name with embedded hot links to 
  -- the description of the stage, and possibly also its parent stage.
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

  define stgNameHtml    varchar(200);
  define stgName        like stage.stg_name;
  define stgUrl         like stage.stg_comments_relative_url;
  define stgNameLength  int;
  define col            int;
  define superStgName	like stage.stg_name;
  define subStgName	like stage.stg_name;
  define superStgUrl    like stage.stg_comments_relative_url;

  select stg_name, stg_comments_relative_url
    into stgName, stgUrl
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
    let stgNameHtml = "UNKNOWN";
  else
    let superStgUrl = NULL;

    if (stgName like "%:%") then
      -- stage name contains a colon.
      let stgNameLength = length(stgName);
      let col  = 1;
      while (substring(stgName from col for 1) <> ":")
        let col = col + 1;
      end while

      let superStgName = substring(stgName from 1 for col - 1);
      let subStgName   = substring(stgName from col + 1);

      select stg_comments_relative_url 
        into superStgUrl
        from stage
	where stg_name = superStgName;
    end if

    if (superStgUrl is NULL) then  -- no super stage
      if (javascriptFunc is NULL) then
        let stgNameHtml = '<a href="' || stgUrl || '">' || stgName || "</a>";
      else
	let stgNameHtml = '<a href="javascript:' || javascriptFunc || "('" ||
			  stgUrl || "')" || '">' || stgName || "</a>";
      end if      
    else
      if (javascriptFunc is NULL) then
        let stgNameHtml = '<a href="' || superStgUrl || '">' || 
			  superStgName ||
			  '</a> : <a href="' || stgUrl || '">' || subStgName ||
			  '</a>';
      else
        let stgNameHtml = '<a href="javascript:' || javascriptFunc || "('" ||
			  superStgUrl || "')" || '">' || superStgName ||
			  '</a> : <a href="javascript:' || javascriptFunc || 
			  "('" || stgUrl || "')" || '">' || subStgName || 
			  "</a>";
      end if
    end if
  end if

  return stgNameHtml;

end function;
