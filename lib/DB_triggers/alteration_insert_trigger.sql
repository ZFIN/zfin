create trigger alteration_insert_trigger 
  insert on alteration 
    referencing new as new_alteration
    for each row (
        execute function zero_pad(new_alteration.allele) 
	  into alteration.alt_allele_order
    );
