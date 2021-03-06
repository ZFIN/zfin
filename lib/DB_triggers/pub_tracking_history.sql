drop trigger if exists pub_tracking_history_trigger on pub_tracking_history;

create or replace function pub_tracking_history()
returns trigger as
$BODY$
begin

       perform updateMaxStatusAsCurrent(NEW.pth_pub_zdb_id);
       NEW.pth_status_is_current = 't';

        perform checkPubTrackingLocationOwner (NEW.pth_pub_zdb_id,
                                                NEW.pth_status_id,
                                                NEW.pth_location_id,
                                                NEW.pth_claimed_by);
        perform updatePubCompletionDate(NEW.pth_pub_zdb_id,
                                                NEW.pth_status_id);
        RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger pub_tracking_history_trigger before insert on pub_tracking_history
 for each row
 execute procedure pub_tracking_history();