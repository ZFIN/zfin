create trigger mrkrgoev_infered_from_update_trigger 
       update of mrkrgoev_infered_from on marker_go_term_evidence 
       referencing old as oldM
                   new as newM
    for each row
      (
	execute function scrub_char ( newM.mrkrgoev_infered_from )
		into mrkrgoev_infered_from
      );
