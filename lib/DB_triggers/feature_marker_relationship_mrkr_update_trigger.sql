create trigger feature_marker_relationship_mrkr_update_trigger 
    update of fmrel_mrkr_zdb_id
    on feature_marker_relationship referencing new as new_ftr_rel
	old as old_ftr_rel
    for each row
        (
        execute procedure p_update_fmrel_genotype_names(
		new_ftr_rel.fmrel_mrkr_zdb_id,
		old_ftr_rel.fmrel_mrkr_zdb_id),
        execute procedure p_fmrel_grpmem_correct(
		new_ftr_rel.fmrel_ftr_zdb_id,
		new_ftr_rel.fmrel_mrkr_zdb_id,
		new_ftr_rel.fmrel_type)	
	);