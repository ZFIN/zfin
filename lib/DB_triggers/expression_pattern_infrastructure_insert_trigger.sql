create trigger expression_pattern_infrastructure_update_trigger 
    update of xpatinf_anatitem_zdb_id,xpatinf_expressed,xpatinf_pub_zdb_id,
    xpatinf_curator_zdb_id on expression_pattern_infrastructure 
    referencing new as new_xpatinf
    for each row
        (
        execute procedure p_check_submitter_is_root(new_xpatinf.xpatinf_curator_zdb_id 
    ));
