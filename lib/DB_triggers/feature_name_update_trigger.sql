create trigger feature_name_update_trigger update of 
    feature_name on feature referencing old as oldf new 
    as newf
      for each row
        (  
	execute function zero_pad(newf.feature_name ) 
        into feature.feature_name_order,
	execute function scrub_char(newf.feature_name ) 
        into feature.feature_name,
	execute procedure p_update_related_genotype_names(
		newf.feature_zdb_id),
	execute procedure fhist_event(newf.feature_zdb_id,
       'reassigned', newf.feature_name,oldf.feature_name) 
);
