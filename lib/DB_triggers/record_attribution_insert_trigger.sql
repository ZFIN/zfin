create trigger record_attribution_insert_trigger insert on 
    record_attribution referencing new as new_ra
    for each row
        (
        execute function scrub_char ( new_ra.recattrib_source_zdb_id )
                into recattrib_source_zdb_id
        );
