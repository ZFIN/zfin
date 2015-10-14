create trigger disease_annotation_insert_trigger insert 
    on disease_annotation referencing new as new_da
    
    for each row
        (
        execute procedure p_disease_annotation_term_is_from_do(new_da.dat_term_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids(
                        new_da.dat_term_zdb_id,
                        new_da.dat_source_zdb_id)

);
