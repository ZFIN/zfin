drop trigger if exists marker_abbrev_trigger on publication;


create or replace function marker_abbrev()
returns trigger as
$BODY$
declare mrkr_abbrev marker.mrkr_abbrev%TYPE;
declare mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE;

begin
     mrkr_abbrev = (select scrub_char(NEW.mrkr_abbrev));     
     NEW.mrkr_abbrev = mrkr_abbrev;
     
     select p_check_mrkr_abbrev(NEW.mrkr_name,
			        NEW.mrkr_abbrev,
				NEW.mrkr_type );

     mrkr_abbrev_order = (select zero_pad(mrkr_abbrev_order));
     NEW.mrkr_abbrev_order = mrkr_abbrev_order;


     select mhist_event(NEW.mrkr_zdb_id,OLD.mrkr_name,
					NEW.mrkr_name, 
					OLD.mrkr_abbrev, 
					NEW.mrkr_abbrev);

     select p_update_related_names(NEW.mrkr_zdb_id,
				   OLD.mrkr_abbrev,
				   NEW.mrkr_abbrev );

     select update_construct_name_component(NEW.mrkr_zdb_id, 
					    NEW.mrkr_abbrev);
     select p_update_related_fish_names(NEW.mrkr_zdb_id);

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_abbrev_trigger before update on marker
 for each row 
 when (OLD.mrkr_abbrev IS DISTINCT FROM NEW.mrkr_abbrev)
 execute procedure marker_abbrev();
