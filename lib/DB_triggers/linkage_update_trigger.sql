create trigger linkage_update_trigger update of lnkg_zdb_id,
    lnkg_source_zdb_id on linkage referencing old as 
    old_linkage new as new_linkage
    for each row
        (
        execute procedure p_insert_into_record_attribution_tablezdbids(new_linkage.lnkg_zdb_id 
    ,new_linkage.lnkg_source_zdb_id ));