create trigger marker_go_term_evidence_insert_trigger insert on 
    marker_go_term_evidence referencing new as new_mrkrgoev
    for each row
        (
	execute procedure p_goterm_not_obsolete (
		new_mrkrgoev.mrkrgoev_go_term_zdb_id),
	execute procedure p_marker_has_goterm (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
   			new_mrkrgoev.mrkrgoev_go_term_zdb_id),
        execute function scrub_char (new_mrkrgoev.mrkrgoev_notes)
                into mrkrgoev_notes,
	execute procedure p_insert_into_record_attribution (
			new_mrkrgoev.mrkrgoev_zdb_id,
			new_mrkrgoev.mrkrgoev_source_zdb_id,
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id)
     );		