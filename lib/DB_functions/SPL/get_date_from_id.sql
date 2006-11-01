create function
get_date_from_id(zdbId varchar(50),format varchar(10))

  returning varchar(10);  

  -- Given a ZDB ID, extracts and returns the date portion
  -- Returns NULL if Object Type is invalid.

  define century	varchar(2);
  define zdb_date	varchar(6);
  define dateFromId	varchar(10);
  define offset		integer;
  define objType	varchar(20);

  let objType = get_obj_type (zdbId);
  let offset = length(objType) + 6;
  let zdb_date = substr(zdbId,offset,6);

  --| Determine Century |--
  if (substr(zdb_date,0,1) = "9") then
      let century = "19";
  else
      let century = "20";
  end if  
  
    
  IF (format = "MM/DD/YYYY") THEN
      -- date format MM/DD/YYYY.
      let dateFromId = substr(zdb_date,3,2) || '/' || substr(zdb_date,5,2) || '/' || century || substr(zdb_date,0,2);

  ELIF (format = "YYYY-MM-DD") THEN
      -- datetime year to day format.
      let dateFromId = century || substr(zdb_date,0,2) || '-' || substr(zdb_date,3,2) || '-' || substr(zdb_date,5,2);
  
  ELSE
      -- date format YYYYMMDD.
      let dateFromId = century || zdb_date || '  ';
  
  END IF

  return dateFromId;

end function;
