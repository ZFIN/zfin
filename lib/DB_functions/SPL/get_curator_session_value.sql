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



create function 
get_curator_session_value(person_zdb_id varchar(50), data_zdb_id varchar(50), field_name varchar(100))
returning lvarchar(1000);

  define field_value lvarchar(1000);
  let field_value = NULL;

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
  end if

  return field_value;

end function;