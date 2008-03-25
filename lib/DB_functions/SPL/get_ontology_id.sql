----------------------------------------------------------------
-- This procedure returns the ontology id for a given ZDB ID.
-- If the object does not have an ontology, then null is returned
-- If ZDB ID does not point to a record, return NULL.
--
-- INPUT VARS:
--              zdbId 
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
--              None
-- RETURNS:
--              successful: object term id
--              fails: NULL
-------------------------------------------------------------

create function
get_ontology_id(zdbId varchar(50))

  returning varchar(255);  -- longest abbrev in DB is 255 characters long.

  -- Given a ZDB ID, gets the id from the appropriate 
  -- Returns NULL if ZDB ID does not point to a record.

  define objType	like zdb_object_type.zobjtype_name;
  define objName	varchar(255);

  let objName = NULL;
  let objType = get_obj_type (zdbId);

  -- list the most likely types first.

  if (objType = "GOTERM") then
    select 'GO:'||goterm_go_id
      into objName
      from go_term
      where goterm_zdb_id = zdbId;
  elif (objType = "ANAT") then
    select anatitem_obo_id
      into objName
      from anatomy_item
      where anatitem_zdb_id= zdbId ;
  end if
  
  return objName;

end function;
