drop trigger mrkr_name_update_trigger;

create trigger mrkr_name_update_trigger 
  update of mrkr_name on marker 
    referencing new as new_marker
    for each row (
        execute function zero_pad(new_marker.mrkr_name) 
	  into marker.mrkr_name_order
    );
