drop trigger marker_insert_trigger;

create trigger marker_insert_trigger
  insert on marker 
    referencing new as new_marker
    for each row (
        execute function zero_pad(new_marker.mrkr_name)
	  into marker.mrkr_name_order,
        execute function zero_pad(new_marker.mrkr_abbrev) 
	  into marker.mrkr_abbrev_order
    );
