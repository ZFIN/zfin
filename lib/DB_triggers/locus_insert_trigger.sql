create trigger locus_insert_trigger 
  insert on locus 
    referencing new as new_locus
    for each row (
        execute function zero_pad(new_locus.abbrev)
	  into locus.locus_abbrev_order
    );
