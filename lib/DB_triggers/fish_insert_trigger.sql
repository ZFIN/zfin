create trigger fish_insert_trigger 
  insert on fish 
    referencing new as new_fish
    for each row (
	execute function scrub_char(new_fish.name)
          into fish.name,
        execute function zero_pad(new_fish.name) 
	  into fish.fish_name_order,
        execute function zero_pad(new_fish.allele) 
	  into fish.fish_allele_order
    );
