drop trigger fish_name_update_trigger;

create trigger fish_name_update_trigger 
  update of name on fish 
    referencing new as new_fish
    for each row (
        execute function zero_pad(new_fish.name) 
	  into fish.fish_name_order
    );
