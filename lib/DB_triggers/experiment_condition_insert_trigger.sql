create trigger experiment_condition_insert_trigger
  insert on experiment_condition
  referencing new as new_expcond
  for each row (
	execute function scrub_char(new_expcond.expcond_comments) 
		into expcond_comments
) ;