create trigger marker_go_term_evidence_update_trigger 
  update of mrkrgoev_mrkr_zdb_id, mrkrgoev_term_zdb_id,
	mrkrgoev_source_zdb_id
  on marker_go_Term_Evidence
  referencing new as new_mrkrgoev
    for each row
	(
	execute procedure p_goterm_not_obsolete (
		new_mrkrgoev.mrkrgoev_term_zdb_id),
        execute function scrub_char ( 
			new_mrkrgoev.mrkrgoev_notes )
                into mrkrgoev_notes,
	-- insure root term not be added to marker with non-root term.
	execute procedure p_marker_has_goterm (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
   			new_mrkrgoev.mrkrgoev_term_zdb_id),

	-- when root term go term is added, non-root term should be deleted
	-- if no root terms, the pub on root term annotation should be deleted
	execute procedure p_check_drop_go_root_term (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
			new_mrkrgoev.mrkrgoev_term_zdb_id),

	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_mrkrgoev.mrkrgoev_zdb_id,
			new_mrkrgoev.mrkrgoev_source_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids (
			new_mrkrgoev.mrkrgoev_mrkr_zdb_id,
			new_mrkrgoev.mrkrgoev_source_zdb_id)
--	execute procedure p_marker_go_term_evidence_infered_composite (
--			new_mrkrgoev.mrkrgoev_zdb_id )
     );			
