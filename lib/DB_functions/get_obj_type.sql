create or replace function get_obj_type(zdbId varchar) returns varchar as $objType$ 

declare objType zdb_object_type.zobjtype_name%TYPE ;

begin   

  objType = substring(zdbId from '-([A-Z]*)-') ;

return objType;

end
$objType$ LANGUAGE plpgsql;

