 create trigger zdb_object_type_update_trigger 
 update of zobjtype_home_table, zobjtype_home_zdb_id_column
  on zdb_object_type
  referencing new as new_object
  for each row
	(execute procedure p_check_zdb_object_table
		(new_object.zobjtype_home_table, new_object.zobjtype_home_zdb_id_column));