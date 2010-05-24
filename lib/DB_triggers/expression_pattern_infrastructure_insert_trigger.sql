create trigger expression_pattern_infrastructure_insert_trigger 
    insert on expression_pattern_infrastructure referencing 
    new as new_xpatinf
    for each row
        (
        execute procedure p_check_submitter_is_root(new_xpatinf.xpatinf_curator_zdb_id),
	execute procedure p_check_fx_postcomposed_terms(new_xpatinf.xpatinf_superterm_zdb_id,new_xpatinf.xpatinf_subterm_zdb_id),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatinf.xpatinf_superterm_zdb_id),
        execute procedure p_term_is_not_obsolete_or_secondary(new_xpatinf.xpatinf_subterm_zdb_id)	 
);