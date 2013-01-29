-- CREATE TERM UPDATE TRIGGER
-------------------------------------------
-- The trigger populates term_name_order

create trigger term_update_trigger
	update on term
	referencing new as new_term
	for each row (
	
	      execute function 
		zero_pad(new_term.term_name) 
		into term_name_order,
		
	      execute function
		scrub_char(new_term.term_name)
		into term_name
	);


