create trigger expression_pattern_update_trigger 
    update of xpat_source_zdb_id 
    on expression_pattern
    referencing new as new_xpat
    for each row
        (
	execute procedure p_insert_into_record_attribution (
			new_xpat.xpat_zdb_id,
			new_xpat.xpat_source_zdb_id,
			new_xpat.xpat_probe_zdb_id),
	execute procedure p_insert_into_record_attribution (
			new_xpat.xpat_zdb_id,
			new_xpat.xpat_source_zdb_id,
			new_xpat.xpat_gene_zdb_id)
     );