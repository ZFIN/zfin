create trigger orthologue_insert_trigger insert on 
    orthologue referencing new as new_ortho
    for each row
        (
	execute function scrub_char ( new_ortho.ortho_name )
		into ortho_name,
	execute function scrub_char ( new_ortho.ortho_abbrev )
		into ortho_abbrev
        );
