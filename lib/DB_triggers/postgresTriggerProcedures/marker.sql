drop trigger if exists marker_trigger on marker;
drop trigger if exists marker_name_order on marker;

create or replace function marker_name_order()
returns trigger as 
$BODY$

declare mrkr_name marker.mrkr_name%TYPE := scrub_char(NEW.mrkr_name);
 mrkr_abbrev marker.mrkr_abbrev%TYPE := scrub_char(NEW.mrkr_abbrev);
 mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE := zero_pad(mrkr_abbrev);
 mrkr_name_order marker.mrkr_name_order%TYPE := scrub_char(zero_pad(NEW.mrkr_name));

begin 

     NEW.mrkr_name = mrkr_name;

     NEW.mrkr_abbrev_order = mrkr_abbrev_order;

     NEW.mrkr_name_order = mrkr_name_order;           
     
     NEW.mrkr_comments = mrkr_comments;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;


create or replace function marker()
returns trigger as
$BODY$
declare mrkr_name marker.mrkr_name%TYPE;
declare mrkr_abbrev marker.mrkr_abbrev%TYPE := updateAbbrevEqualName(NEW.mrkr_zdb_id, 
     	    			   NEW.mrkr_name, 
				   NEW.mrkr_type, 
				   NEW.mrkr_abbrev);
declare mrkr_comments marker.mrkr_comments%TYPE;


begin
     
     NEW.mrkr_abbrev = mrkr_abbrev;
 
     

     perform p_check_mrkr_abbrev(NEW.mrkr_name,
				NEW.mrkr_abbrev,
				NEW.mrkr_type );

     perform mhist_event(NEW.mrkr_zdb_id, '',NEW.mrkr_abbrev, '',NEW.mrkr_name);

     perform p_populate_go_root_terms(NEW.mrkr_zdb_id,
				     NEW.mrkr_name,
				     NEW.mrkr_type);

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_name_order_trigger before insert on marker
 for each row
 execute procedure marker_name_order();

create trigger marker_trigger after insert on marker
 for each row
 execute procedure marker();
