create trigger locusreg_abbrev_update_trigger 
  update of abbrev on locus_registration
    referencing new as new_locusreg
    for each row (
	execute function scrub_char(new_locusreg.abbrev)
	  into abbrev
    );
