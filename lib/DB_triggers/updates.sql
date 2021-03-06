drop trigger if exists updates_trigger on updates;

create or replace function updates()
returns trigger as
$BODY$
declare rec_id text := scrub_char(NEW.rec_id);
begin
     
     NEW.rec_id = rec_id;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger updates_trigger after insert or update on updates
 for each row
 execute procedure updates();
