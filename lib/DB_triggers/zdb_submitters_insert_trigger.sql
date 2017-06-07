create trigger zdb_submitters_insert_trigger before insert on 
    zdb_submitters referencing new as new_zdb_submitters
    for each row
        (
	execute procedure zdb_submitters_insert ( new_zdb_submitters.name )
        );
