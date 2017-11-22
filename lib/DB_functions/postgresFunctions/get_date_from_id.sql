create or replace function get_date_from_id(zdbId varchar,format varchar) returns varchar as $dateFromId$ 

  -- Given a ZDB ID, extracts and returns the date portion
  -- Returns NULL if Object Type is invalid.
declare
   century	varchar;
   dateFromId	varchar;
   objType	varchar := get_obj_type(zdbId);
   zdb_date	varchar ;

begin

  zdb_date := substring(zdbId from length(objType)+6 for 6);

  --| Determine Century |--
  if (substr(zdb_date,0,1) = '9') then
       century := '19';
  else
      century := '20';
  end if;
  
    
  IF (format = 'MM/DD/YYYY') THEN
      -- date format MM/DD/YYYY.
      dateFromId := substr(zdb_date,3,2) || '/' || substr(zdb_date,5,2) || '/' || century || substr(zdb_date,0,2);

  ELSIF (format = 'YYYY-MM-DD') THEN
      -- datetime year to day format.
      dateFromId := century || substr(zdb_date,1,2) || '-' || substr(zdb_date,3,2) || '-' || substr(zdb_date,5,2);
  
  ELSE
      -- date format YYYYMMDD.
      dateFromId := century || zdb_date || '  ';
  
  END IF;

  return dateFromId;

end
$dateFromId$ LANGUAGE plpgsql;
