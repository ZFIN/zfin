create trigger mrkr_name_update_trigger 
       update of mrkr_name on marker 
       referencing old as oldM
                   new as newM
    for each row
      (
	execute function scrub_char ( newM.mrkr_name )
		into marker.mrkr_name,
	execute procedure p_check_mrkr_abbrev (newM.mrkr_name, newM.mrkr_abbrev,
					       newM.mrkr_type),
        execute function zero_pad(newM.mrkr_name)
                into marker.mrkr_name_order,
        execute procedure mhist_event (newM.mrkr_zdb_id,'renamed',
                newM.mrkr_name, oldM.mrkr_name),
	execute procedure p_update_related_genotype_names (newM.mrkr_zdb_id)
      );
