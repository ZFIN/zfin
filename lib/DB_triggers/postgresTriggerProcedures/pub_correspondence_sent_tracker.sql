drop trigger if exists pub_correspondence_sent_tracker_trigger on pub_correspondence_sent_tracker;

create or replace function pub_correspondence_sent_tracker()
returns trigger as
$BODY$
begin
   perform updatePubLastSentEmailDate(NEW.pubcst_pub_zdb_id,NEW.pubcst_date_sent);
   RETURN NULL;
end;
$BODY$ LANGUAGE plpgsql;

create trigger pub_correspondence_sent_tracker_trigger before insert or update on pub_correspondence_sent_tracker
 for each row
 execute procedure pub_correspondence_sent_tracker();
