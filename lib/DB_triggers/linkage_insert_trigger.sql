create trigger linkage_insert_trigger insert on "informix"
    .linkage referencing new as new_linkage
    for each row
        (
        execute procedure p_insert_into_record_attribution_tablezdbids(new_linkage.lnkg_zdb_id 
    ,new_linkage.lnkg_source_zdb_id ));