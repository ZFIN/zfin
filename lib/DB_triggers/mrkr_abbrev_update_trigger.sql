create trigger mrkr_abbrev_update_trigger update of 
    mrkr_abbrev on marker referencing old as oldm new 
    as newm
    for each row
        (
        execute function scrub_char(newm.mrkr_abbrev 
    ) into marker.mrkr_abbrev,
        execute procedure p_check_mrkr_abbrev(newm.mrkr_name 
    ,newm.mrkr_abbrev ,newm.mrkr_type ),
        execute function zero_pad(newm.mrkr_abbrev 
    ) into marker.mrkr_abbrev_order,
        execute procedure mhist_event(newm.mrkr_zdb_id,oldm.mrkr_name,
					newm.mrkr_name, oldm.mrkr_abbrev, 
					newm.mrkr_abbrev ),
        execute procedure p_update_related_names(newm.mrkr_zdb_id 
    ,oldm.mrkr_abbrev ,newm.mrkr_abbrev ),
    execute procedure update_construct_name_component(newm.mrkr_zdb_id, newm.mrkr_abbrev),
    execute procedure p_update_related_fish_names(newm.mrkr_zdb_id));

