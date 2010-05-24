create trigger expression_result_update_trigger update 
    of xpatres_start_stg_zdb_id,xpatres_end_stg_zdb_id,xpatres_superterm_zdb_id,
       xpatres_subterm_zdb_id on expression_result 
    referencing new as new_xpatres
    for each row
        (
        execute procedure p_stg_hours_consistent(new_xpatres.xpatres_start_stg_zdb_id,
						new_xpatres.xpatres_end_stg_zdb_id ),
        execute function scrub_char(new_xpatres.xpatres_comments) 
	      into expression_result.xpatres_comments,
        execute procedure p_check_fx_postcomposed_terms(new_xpatres.xpatres_superterm_zdb_id,
					new_xpatres.xpatres_subterm_zdb_id),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_superterm_zdb_id),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatres.xpatres_subterm_zdb_id)
);