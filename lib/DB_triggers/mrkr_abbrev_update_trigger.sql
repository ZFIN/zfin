drop trigger mrkr_abbrev_update_trigger;

create trigger mrkr_abbrev_update_trigger 
  update of mrkr_abbrev on marker 
    referencing new as new_marker
    for each row (
        execute function zero_pad(new_marker.mrkr_abbrev)
	  into marker.mrkr_abbrev_order
    );
