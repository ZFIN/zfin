 create trigger zdb_object_type_insert_trigger insert 
  on zdb_object_type 
  referencing new as new_object
  for each row 
 	(execute procedure p_check_zdb_object_table
		(new_object.zobjtype_home_table,
		 new_object.zobjtype_home_zdb_id_column));
