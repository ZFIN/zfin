create trigger external_reference_insert_trigger insert on
    external_reference referencing new as new_exref
    for each row
    (
        execute function scrub_char(new_exref.exref_reference) 
	  into exref_reference,
	  
        execute procedure p_insert_into_record_attribution_datazdbids (
		new_exref.exref_data_zdb_id, new_exref.exref_reference)

    );