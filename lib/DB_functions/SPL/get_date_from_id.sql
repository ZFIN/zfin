drop function get_date_from_id;

create function
get_date_from_id(zdbId varchar(50))

  returning varchar(8);  -- date format YYYYMMDD.

  -- Given a ZDB ID, gets the name of the object associated with that ZDB ID.
  -- If the object does not have a name per se, then its ZDB ID is returned
  --   as the name.
  -- Returns NULL if ZDB ID does not point to a record.

  define century	varchar(2);
  define zdb_date	varchar(6);
  define dateFromId	varchar(8);
  define offset		integer;
  define objType	varchar(20);

  let objType = get_obj_type (zdbId);
  let offset = length(objType) + 6;
  let zdb_date = substr(zdbId,offset,6);
  
  if (substr(zdb_date,0,1) = "9") then
      let century = "19";
  else
      let century = "20";
  end if

  let dateFromId = century || zdb_date;

  return dateFromId;

end function;

update statistics for function get_date_from_id;