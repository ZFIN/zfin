drop function get_date_from_id;

create function
get_date_from_id(zdbId varchar(50))

  returning varchar(8);  -- date format YYYYMMDD.

  -- Given a ZDB ID, gets the name of the object associated with that ZDB ID.
  -- If the object does not have a name per se, then its ZDB ID is returned
  --   as the name.
  -- Returns NULL if ZDB ID does not point to a record.

  define d		varchar(8);
  define offset		integer;
  define objType	varchar(20);

  let objType = get_obj_type (zdbId);
  let offset = length(objType) + 6;
  let d = "20" || substr(zdbId,offset,6);

  return d;

end function;

update statistics for function get_date_from_id;