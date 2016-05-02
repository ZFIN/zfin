create trigger external_note_update_trigger 
    update of extnote_data_zdb_id, extnote_source_zdb_id 
    on external_note referencing new as new_extnote
    for each row (
	execute procedure p_insert_into_record_attribution_tablezdbids(new_extnote.extnote_zdb_id 
    ,new_extnote.extnote_source_zdb_id ),
        execute procedure p_insert_into_record_attribution_datazdbids(new_extnote.extnote_data_zdb_id 
    ,new_extnote.extnote_source_zdb_id ));
