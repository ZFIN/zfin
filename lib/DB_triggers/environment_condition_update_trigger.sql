create trigger environment_condition_update_trigger
   update of envcond_comments 
   on environment_condition
   referencing new as new_envcond
   for each row 
	(
	execute function scrub_char(new_envcond.envcond_comments) 
		into envcond_comments
) ;