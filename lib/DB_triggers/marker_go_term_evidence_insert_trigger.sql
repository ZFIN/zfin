create trigger marker_go_term_evidence_insert_trigger insert on 
    marker_go_term_evidence referencing new as new_mrkrgoev
    for each row
        (
	execute function scrub_char ( new_mrkrgoev.mrkrgoev_infered_from )
		into mrkrgoev_infered_from,
	execute function scrub_char ( new_mrkrgoev.mrkrgoev_notes )
		into mrkrgoev_notes
        );
