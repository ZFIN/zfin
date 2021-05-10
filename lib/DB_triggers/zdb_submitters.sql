drop trigger if exists zdb_submitters_trigger on zdb_submitters;

create or replace function zdb_submitters()
returns trigger as
$BODY$
declare name text := scrub_char(NEW.name);
begin
     
     NEW.name = name;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger zdb_submitters_trigger after insert or update on zdb_submitters
 for each row
 execute procedure zdb_submitters();
