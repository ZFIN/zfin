create trigger column_attribution_insert_trigger insert on 
    column_attribution referencing new as new_ca
    for each row
        (
        execute function scrub_char ( new_ca.colattrib_source_zdb_id )
                into colattrib_source_zdb_id
        );
