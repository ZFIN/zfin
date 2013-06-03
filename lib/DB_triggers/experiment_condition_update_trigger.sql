create trigger experiment_condition_update_trigger
   update of expcond_comments 
   on experiment_condition
   referencing new as new_expcond
   for each row 
	(
	execute function scrub_char(new_expcond.expcond_comments) 
		into expcond_comments,
	execute function scrub_char(new_expcond.expcond_value)
		into expcond_value
) ;