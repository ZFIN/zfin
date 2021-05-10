drop trigger if exists marker_abbrev_trigger on marker;
drop trigger if exists marker_abbrev_insert_trigger on marker;
drop trigger if exists marker_abbrev_update_trigger on marker;
drop trigger if exists marker_abbrev_after_update_trigger on marker;

create or replace function marker_abbrev_insert()
returns trigger as
$BODY$

declare mrkr_abbrev marker.mrkr_abbrev%TYPE;
	mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE;
        mrkr_name marker.mrkr_name%TYPE;

begin

     mrkr_name = scrub_char(NEW.mrkr_name);
     mrkr_abbrev = scrub_char(NEW.mrkr_abbrev);
     mrkr_abbrev_order = zero_pad(NEW.mrkr_abbrev_order);

     NEW.mrkr_abbrev_order = mrkr_abbrev_order;
     NEW.mrkr_abbrev = mrkr_abbrev;
     NEW.mrkr_name = mrkr_name;

     perform p_check_mrkr_abbrev(NEW.mrkr_name,
			        NEW.mrkr_abbrev,
				NEW.mrkr_type );

     raise notice 'mrkr_abbrev_order: %', mrkr_abbrev_order;
     raise notice 'mrkr_abbrev_order: %', NEW.mrkr_abbrev_order;


     perform update_construct_name_component(NEW.mrkr_zdb_id, 
					    NEW.mrkr_abbrev);
     perform p_update_related_fish_names(NEW.mrkr_zdb_id);

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create or replace function marker_abbrev_after_update()
returns trigger as 
$BODY$

begin 
  
    perform p_update_related_names(OLD.mrkr_zdb_id,
                                   NEW.mrkr_abbrev,
                                   NEW.mrkr_abbrev);

    perform update_construct_name_component(NEW.mrkr_zdb_id,
                                            NEW.mrkr_abbrev);
     perform p_update_related_fish_names(NEW.mrkr_zdb_id);
   RETURN NEW;
end;

$BODY$ LANGUAGE plpgsql;

create or replace function marker_abbrev_update()
returns trigger as
$BODY$

declare mrkr_abbrev marker.mrkr_abbrev%TYPE;
	mrkr_abbrev_order marker.mrkr_abbrev_order%TYPE;
        mrkr_name marker.mrkr_name%TYPE;
    
begin

     mrkr_name = scrub_char(NEW.mrkr_name);
     mrkr_abbrev = scrub_char(NEW.mrkr_abbrev);
     
     mrkr_abbrev_order = zero_pad(mrkr_abbrev);

     NEW.mrkr_abbrev_order = mrkr_abbrev_order;
     NEW.mrkr_abbrev = mrkr_abbrev;
     NEW.mrkr_name = mrkr_name;

     perform p_check_mrkr_abbrev(NEW.mrkr_name,
			        NEW.mrkr_abbrev,
				NEW.mrkr_type );

  --   raise notice 'mrkr_abbrev_order: %', mrkr_abbrev_order;
  --   raise notice 'mrkr_abbrev_order: %', NEW.mrkr_abbrev_order;

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;



create trigger marker_abbrev_insert_trigger before insert on marker
 for each row 
 execute procedure marker_abbrev_insert();


create trigger marker_abbrev_update_trigger before update of mrkr_abbrev on marker
 for each row 
 execute procedure marker_abbrev_update();

create trigger marker_abbrev_after_update_trigger after update of mrkr_abbrev on marker
 for each row
 execute procedure marker_abbrev_after_update();


