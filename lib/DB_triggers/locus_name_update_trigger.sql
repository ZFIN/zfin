create trigger locus_name_update_trigger
    update of locus_name on locus 
    referencing new as new_locus
    for each row (
	execute function scrub_char(new_locus.locus_name)
	  into locus_name
    );
