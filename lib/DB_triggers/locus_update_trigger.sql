create trigger locus_update_trigger
    update of locus_name, abbrev on locus 
    referencing new as new_locus
    for each row (
	execute function scrub_char(new_locus.abbrev)
	  into abbrev,
	execute function scrub_char(new_locus.locus_name)
	  into locus_name,
        execute function zero_pad(new_locus.abbrev) 
	  into locus.locus_abbrev_order
    );

