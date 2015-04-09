
create trigger construct_component_update_trigger 
 update of cc_component_zdb_id, cc_component on construct_component
 referencing old as old_component
 	     new as new_component
    for each row (
    	execute procedure update_construct_name (new_component.cc_construct_zdb_id, new_component.cc_component_zdb_id)
); 
