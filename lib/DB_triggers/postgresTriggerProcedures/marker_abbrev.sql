drop trigger if exists marker_abbrev_trigger on marker;


create or replace function marker_abbrev()
returns trigger as
$BODY$
declare mrkr_abbrev marker.mrkr_abbrev%TYPE := scrub_char(NEW.mrkr_abbrev);
declare mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE := zero_pad(mrkr_abbrev_order);

begin

     NEW.mrkr_abbrev = mrkr_abbrev;
     
     perform p_check_mrkr_abbrev(NEW.mrkr_name,
			        NEW.mrkr_abbrev,
				NEW.mrkr_type );


     NEW.mrkr_abbrev_order = mrkr_abbrev_order;


     perform mhist_event(NEW.mrkr_zdb_id,OLD.mrkr_name,
					NEW.mrkr_name, 
					OLD.mrkr_abbrev, 
					NEW.mrkr_abbrev);

     perform p_update_related_names(NEW.mrkr_zdb_id,
				   OLD.mrkr_abbrev,
				   NEW.mrkr_abbrev );

     perform update_construct_name_component(NEW.mrkr_zdb_id, 
					    NEW.mrkr_abbrev);
     perform p_update_related_fish_names(NEW.mrkr_zdb_id);

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_abbrev_trigger before update on marker
 for each row 
 when (OLD.mrkr_abbrev IS DISTINCT FROM NEW.mrkr_abbrev)
 execute procedure marker_abbrev();
