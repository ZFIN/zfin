create trigger expression_pattern_infrastructure_update_trigger 
   update on expression_pattern_infrastructure referencing 
    new as new_xpatinf
    for each row
        (
        execute procedure p_check_submitter_is_root(new_xpatinf.xpatinf_curator_zdb_id 
    ));
