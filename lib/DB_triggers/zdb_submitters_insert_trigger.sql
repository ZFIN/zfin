create trigger zdb_submitters_insert_trigger insert on 
    zdb_submitters referencing new as new_zdb_submitters
    for each row
        (
	execute function scrub_char(new_zdb_submitters.name)
        	into name);
