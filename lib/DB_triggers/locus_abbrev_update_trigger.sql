create trigger locus_abbrev_update_trigger 
  update of abbrev on locus 
    referencing new as new_locus
    for each row (
	execute function scrub_char(new_locus.abbrev)
          into abbrev,
        execute function zero_pad(new_locus.abbrev)
	  into locus.locus_abbrev_order
    );
