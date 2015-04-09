create trigger mrkr_name_update_trigger update of 
    mrkr_name on marker referencing old as oldm new 
    as newm
    for each row
        (
        execute function scrub_char(newm.mrkr_name 
    ) into marker.mrkr_name,
   	execute function updateAbbrevEqualName (newm.mrkr_zdb_id, newm.mrkr_name, newm.mrkr_type, newm.mrkr_abbrev)
	into marker.mrkr_abbrev,
        execute procedure p_check_mrkr_abbrev(newm.mrkr_name 
    ,newm.mrkr_abbrev ,newm.mrkr_type ),
        execute function zero_pad(newm.mrkr_name ) 
    into marker.mrkr_name_order,
        execute procedure mhist_event(newm.mrkr_zdb_id 
    ,'renamed' ,newm.mrkr_name ,oldm.mrkr_name ));
