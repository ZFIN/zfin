create trigger fish_allele_update_trigger 
  update of allele on fish 
    referencing new as new_fish
    for each row (
        execute function zero_pad(new_fish.allele)
	  into fish.fish_allele_order
    );
