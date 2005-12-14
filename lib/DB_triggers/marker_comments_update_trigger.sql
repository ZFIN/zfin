create trigger marker_comments_update_trigger 
       update of mrkr_comments on marker 
       referencing old as oldM
                   new as newM
    for each row
      (
	execute function scrub_char ( newM.mrkr_comments )
		into marker.mrkr_comments
      );
