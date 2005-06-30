create trigger expression_result_update_trigger update of 
    xpatres_start_stg_zdb_id, xpatres_end_stg_zdb_id
    on expression_result
    referencing new as new_xpatres
    for each row
        (
	execute procedure p_stg_hours_consistent(
			new_xpatres.xpatres_start_stg_zdb_id,
			new_xpatres.xpatres_end_stg_zdb_id ),
	execute function scrub_char ( new_xpatres.xpatres_comments )
		into xpatres_comments
     ) ;
