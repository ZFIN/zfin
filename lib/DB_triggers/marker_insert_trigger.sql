create trigger marker_insert_trigger insert on 
    marker referencing new as new_marker
    for each row
        (
	execute function scrub_char ( new_marker.mrkr_name )
				into marker.mrkr_name,
	execute function scrub_char ( new_marker.mrkr_abbrev )
				into marker.mrkr_abbrev,
	execute function scrub_char ( new_marker.mrkr_comments )
				into marker.mrkr_comments,
        execute function zero_pad ( new_marker.mrkr_name )
                into marker.mrkr_name_order,
        execute function zero_pad ( new_marker.mrkr_abbrev ) 
                into marker.mrkr_abbrev_order,
 	execute procedure p_check_mrkr_abbrev (new_marker.mrkr_name, 
					       new_marker.mrkr_abbrev, 
					       new_marker.mrkr_type),
	execute procedure mhist_event( new_marker.mrkr_zdb_id,
                'assigned', new_marker.mrkr_name,
                new_marker.mrkr_abbrev ),
        execute procedure p_populate_go_root_terms (new_marker.mrkr_zdb_id,
				new_marker.mrkr_name, new_marker.mrkr_type)
--	,
--	execute procedure p_insert_clone_from_marker (new_marker.mrkr_zdb_id,
--			  			     new_marker.mrkr_type)
        );
