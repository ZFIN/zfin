create function
get_stg_name_html(
  stgZdbId        like stage.stg_zdb_id,
  javascriptFunc  varchar(80) default NULL)

  returning varchar(200);

  -- Generates a string containing the stage name with embedded hot links to 
  -- the stage index page except the Unknown stage. 
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

  define stgNameHtml    varchar(200);
  define stgName        like stage.stg_name;
  define col            int;
  define stgUrl		varchar(60);
  define stgNameAnchor	varchar(60);

  let stgUrl = "/zf_info/zfbook/stages/index.html";

  select stg_name
    into stgName
    from stage
    where stg_zdb_id = stgZdbId;

  if (stgName is NULL) then
    let stgNameHtml = "UNKNOWN";
  elif (stgName = "Unknown") then
    let stgNameHtml = stgName;
  else
      let col  = 1;
      while (substring(stgName from col for 1) <> ":"
	 AND substring(stgName from col for 1) <> " " )
        let col = col + 1;
      end while

      let stgNameAnchor = substring(stgName from 1 for col - 1);

      if (javascriptFunc is NULL) then
        let stgNameHtml = '<a href="' || stgUrl || "#" || stgNameAnchor || '">' || stgName || "</a>";
      else
	let stgNameHtml = '<a href="javascript:' || javascriptFunc || "('" ||
			  stgUrl || "#" || stgNameAnchor || "')" || '">' || stgName || "</a>";
      end if      

  end if

  return stgNameHtml;

end function;
