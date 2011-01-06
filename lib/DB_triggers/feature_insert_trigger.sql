create trigger feature_insert_trigger insert on feature
  referencing new as newf
    for each row
        (execute function scrub_char(newf.feature_name) into feature.feature_name,
	 execute function scrub_char(newf.feature_abbrev) into feature.feature_abbrev,	 
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
        execute function zero_pad(newf.feature_name 
    		) into feature.feature_name_order,
        execute function zero_pad(newf.feature_abbrev 
    		) into feature.feature_abbrev_order,
        execute procedure fhist_event(newf.feature_zdb_id,
       		'assigned', newf.feature_name,newf.feature_abbrev),
    execute procedure checkDupFeaturePrefixLineDesignation (newf.feature_lab_prefix_id, newf.feature_line_number) 
);
