create trigger mrkrgoev_notes_update_trigger 
       update of mrkrgoev_notes on marker_go_term_evidence 
       referencing old as oldM
                   new as newM
    for each row
      (
	execute function scrub_char ( newM.mrkrgoev_notes )
		into mrkrgoev_notes
      );
