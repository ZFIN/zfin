drop trigger if exists marker_trigger on marker;

create or replace function marker()
returns trigger as
$BODY$
declare mrkr_name marker.mrkr_name%TYPE;
declare mrkr_abbrev marker.mrkr_abbrev%TYPE;
declare mrkr_comments marker.mrkr_comments%TYPE;
declare mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE;
declare mrkr_name_order marker.mrkr_name_order%TYPE;

begin
     mrkr_name = (select scrub_char(NEW.mrkr_name));
     NEW.mrkr_name = mrkr_name;

     mrkr_abbrev = (select scrub_char(NEW.mrkr_abbrev));
     mrkr_abbrev = (select updateAbbrevEqualName (NEW.mrkr_zdb_id, 
     	    			   NEW.mrkr_name, 
				   NEW.mrkr_type, 
				   NEW.mrkr_abbrev));

     NEW.mrkr_abbrev = mrkr_abbrev;
 
     mrkr_abbrev_order = (Select zero_pad(NEW.mrkr_abbrev));
     NEW.mrkr_abbrev_order = mrkr_abbrev_order;

     mrkr_name_order = (Select zero_pad(NEW.mrkr_name));
     NEW.mrkr_name_order = mrkr_name_order;           
     
     mrkr_comments = (select scrub_char(NEW.mrkr_comments));
     NEW.mrkr_comments = mrkr_comments;

     select p_check_mrkr_abbrev(NEW.mrkr_name,
				NEW.mrkr_abbrev,
				NEW.mrkr_type );
     select mhist_event(NEW.mrkr_zdb_id, '',NEW.mrkr_abbrev, '',NEW.mrkr_name);

     select p_populate_go_root_terms(NEW.mrkr_zdb_id,
				     NEW.mrkr_name,
				     NEW.mrkr_type);

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger marker_trigger before insert on marker
 for each row
 execute procedure marker();
