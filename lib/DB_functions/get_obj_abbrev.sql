----------------------------------------------------------------
-- This procedure returns the abbreviation for a given ZDB ID.
-- If the object does not have an abbreviation, then 
-- get_obj_name(zdbId) is returned instead.
-- If ZDB ID does not point to a record, return NULL.
--
-- INPUT VARS:
--              zdbId 
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
--              call get_obj_name if no abbreviation exists
-- RETURNS:
--              successful: object abbreviation
--              fails: NULL
-------------------------------------------------------------

create or replace function get_obj_abbrev(zdbId varchar) returns varchar as $objAbbrev$

  -- Given a ZDB ID, gets the abbrev of the object associated with that ZDB ID.
  -- If the object does not have an abbrev per se, then get_obj_name is
  --   called, if get_obj_name returns the ZDB_ID, then we ???.
  -- Returns NULL if ZDB ID does not point to a record.


  
  declare objType	zdb_object_type.zobjtype_name%TYPE := substring(zdbId from '-([A-Z]*)-') ;
          objName	varchar := NULL;


  -- list the most likely types first.
 begin

  if (objType in (select marker_type
    from marker_types)) then
    select mrkr_abbrev 
      into objName
      from marker
      where mrkr_zdb_id = zdbId;
  elsif (objType = 'GENO') then
    select geno_display_name
      into objName
      from genotype
      where geno_zdb_id = zdbId;
  elsif (objType = 'JRNL') then
    select jrnl_abbrev  -- NULL values
      into objName
      from journal
      where jrnl_zdb_id = zdbId;
  elsif (objType = 'ORTHO') then
    select ortho_other_species_symbol  -- NULL values 
      into objName
      from ortholog
      where zdb_id = zdbId;
  elsif (objType = 'REFCROSS') then
    select abbrev
      into objName
      from panels		-- Note: this is a fast search table.
      where zdb_id = zdbId;
  elsif (objType = 'STAGE') then
    select stg_abbrev  -- NULL values
      into objName
      from stage
      where stg_zdb_id = zdbId;
  end if;
  
  if (objName is NULL) then
     objName := get_obj_name(zdbId);
  end if;

  return objName;
end

$objAbbrev$ LANGUAGE plpgsql;

