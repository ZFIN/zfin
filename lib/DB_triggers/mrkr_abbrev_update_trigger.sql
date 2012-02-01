create trigger mrkr_abbrev_update_trigger 
       update of mrkr_abbrev on marker 
       referencing old as oldM
                   new as newM
    for each row
      (
	execute function scrub_char ( newM.mrkr_abbrev )
		into marker.mrkr_abbrev,
	execute procedure p_check_mrkr_abbrev (newM.mrkr_name, newM.mrkr_abbrev,
					       newM.mrkr_type),
        execute function zero_pad(newM.mrkr_abbrev)
                into marker.mrkr_abbrev_order,
        execute procedure mhist_event (newM.mrkr_zdb_id,'reassigned',
                newM.mrkr_abbrev, oldM.mrkr_abbrev),
	execute procedure p_update_related_feature_names (newM.mrkr_zdb_id, oldM.mrkr_abbrev, newM.mrkr_abbrev)
      );
