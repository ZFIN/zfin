
create trigger feature_name_update_trigger update of 
    feature_name on feature referencing old as oldf new 
    as newf
      for each row
        (execute function scrub_char(newf.feature_name)
        	into feature.feature_name, 
	execute procedure checkFeatureAbbrev(newf.feature_zdb_id,
       		 		     newf.feature_type, 
       		 		     newf.feature_abbrev, 
				     newf.feature_lab_prefix_id, 
				     newf.feature_line_number,
				     newf.feature_mrkr_abbrev, 
				     newf.feature_df_transloc_complex_prefix,
				     newf.feature_dominant, 
				     newf.feature_unspecified,
				     newf.feature_unrecovered,
				     newf.feature_tg_suffix,
				     newf.feature_known_insertion_site),
	execute function zero_pad(newf.feature_name) 
        	into feature.feature_name_order,
	execute procedure p_update_related_genotype_names(
		newf.feature_zdb_id),
	execute procedure fhist_event(newf.feature_zdb_id,
       		'reassigned', newf.feature_name,oldf.feature_name),
    execute procedure checkDupFeaturePrefixLineDesignation (newf.feature_lab_prefix_id, newf.feature_line_number) 
);