create trigger marker_go_term_evidence_update_trigger 
  update of mrkrgoev_mrkr_zdb_id, mrkrgoev_go_term_zdb_id, 
	mrkrgoev_source_zdb_id
  on marker_go_Term_Evidence
  referencing new as new_mrkrgoev
    for each row
	(
	execute procedure p_goterm_not_obsolete (
		new_mrkrgoev.mrkrgoev_go_term_zdb_id),
        execute function scrub_char ( 
			new_mrkrgoev.mrkrgoev_notes )
                into mrkrgoev_notes,
	execute procedure p_marker_has_goterm (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
   			new_mrkrgoev.mrkrgoev_go_term_zdb_id),
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_mrkrgoev.mrkrgoev_zdb_id,
			new_mrkrgoev.mrkrgoev_source_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
			new_mrkrgoev.mrkrgoev_source_zdb_id)	
     );			
