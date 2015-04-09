
create trigger construct_name_update_trigger 
  update on construct referencing old as old_c new as new_c
  for each row ( execute function scrub_char(new_c.construct_name) 
    into construct_name
--,
--		execute function update_date_modified (new_c.construct_zdb_id)
--		into construct.construcT_date_modified
		 
  );