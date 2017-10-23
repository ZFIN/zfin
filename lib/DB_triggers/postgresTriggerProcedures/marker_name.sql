drop trigger if exists marker_name_trigger on marker;


create or replace function marker_name()
returns trigger as
$BODY$
declare mrkr_name marker.mrkr_name%TYPE := scrub_char(NEW.mrkr_name);
declare	mrkr_name_order marker.mrkr_name_order%TYPE := zero_pad(NEW.mrkr_name);
begin
     
     NEW.mrkr_name = mrkr_name;
     
     perform updateAbbrevEqualName (NEW.mrkr_zdb_id, 
     	    			   NEW.mrkr_name, 
				   NEW.mrkr_type,
     	    			   NEW.mrkr_abbrev);
     perform p_check_mrkr_abbrev (NEW.mrkr_name,
     	    			 NEW.mrkr_abbrev,
				 NEW.mrkr_type);
     

     NEW.mrkr_name_order = mrkr_name_order;

     perform p_update_related_fish_names(NEW.mrkr_zdb_id);
     perform mhist_event(NEW.mrkr_zdb_id,OLD.mrkr_name,
					NEW.mrkr_name, OLD.mrkr_abbrev, 
					NEW.mrkr_abbrev );

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger marker_name_trigger after update on marker
 for each row 
 when (OLD.mrkr_name IS DISTINCT FROM NEW.mrkr_name)
 execute procedure marker_name();
