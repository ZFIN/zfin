drop trigger alteration_allele_update_trigger;

create trigger alteration_allele_update_trigger 
  update of allele on alteration 
    referencing new as new_alteration
    for each row (
        execute function zero_pad(new_alteration.allele) 
	  into alteration.alt_allele_order
    );
