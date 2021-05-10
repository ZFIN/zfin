-- quicky function to get curator session information from the database
--
-- params:
--   person_zdb_id     curator zdb_id, requried
--   data_zdb_id       data zdb_id, optional (some values are for all pubs, some are specific)
--                     when there's no data field, pass in NULL.  
--   field_name        name of field with stored value;
--
--   examples:  
--     execute function get_curator_session_value('ZDB-PERS-000000-1','ZDB-PUB-000000-1','myfield');
--     execute function get_curator_session_value('ZDB-PERS-000000-1',NULL,'otherfield');
--



create or replace function get_curator_session_value(person_zdb_id varchar, data_zdb_id varchar, field_name varchar) returns varchar as $fieldValue$


  declare field_value varchar :=NULL;
  begin
  if (data_zdb_id is NULL) then
    select cs_field_name_value 
      into field_value
      from curator_session
     where cs_person_zdb_id = person_zdb_id 
       and cs_field_name = field_name;
  else 
    select cs_field_name_value 
      into field_value
      from curator_session
    where cs_person_zdb_id = person_zdb_id 
       and cs_data_zdb_id = data_zdb_id
       and cs_field_name = field_name;
  end if;

  return field_value;
 end
$fieldValue$ LANGUAGE plpgsql;
