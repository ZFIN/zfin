create trigger locus_registration_insert_trigger 
  insert on locus_registration
    referencing new as new_locusreg
    for each row (
	execute function scrub_char(new_locusreg.locus_name)
          into locus_name,
	execute function scrub_char(new_locusreg.abbrev)
          into abbrev
    );
