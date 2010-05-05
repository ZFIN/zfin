create trigger feature_marker_relationship_insert_trigger 
    insert on feature_marker_relationship referencing new as 
    new_ftr_rel
    for each row
        (
        execute procedure p_fmrel_grpmem_correct(new_ftr_rel.fmrel_ftr_zdb_id,
		new_ftr_rel.fmrel_mrkr_zdb_id,
		new_ftr_rel.fmrel_type ),
        execute procedure p_markers_present_absent_exclusive(new_ftr_rel.fmrel_mrkr_zdb_id, 
								new_ftr_rel.fmrel_ftr_zdb_id, 
								new_ftr_rel.fmrel_type),
	execute procedure p_update_unspecified_alleles(new_ftr_rel.fmrel_mrkr_zdb_id,
							new_ftr_rel.fmrel_ftr_zdb_id),
	execute procedure p_update_related_genotype_names(new_ftr_rel.fmrel_ftr_zdb_id));