create trigger locusreg_name_update_trigger 
  update of locus_name on locus_registration
    referencing new as new_locusreg
    for each row (
	execute function scrub_char(new_locusreg.locus_name)
	  into locus_name
    );
