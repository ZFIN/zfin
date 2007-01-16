create trigger feature_abbrev_update_trigger update of 
    feature_abbrev on feature referencing old as oldf new 
    as newf
    for each row
        (
        execute function zero_pad(newf.feature_abbrev ) 
	    into feature.feature_abbrev_order,
        execute function scrub_char(newf.feature_abbrev ) 
	    into feature.feature_abbrev,
	execute procedure p_update_related_genotype_names(
		newf.feature_zdb_id),
	execute procedure fhist_event(newf.feature_zdb_id,
       'reassigned', newf.feature_abbrev,oldf.feature_abbrev) 
);
