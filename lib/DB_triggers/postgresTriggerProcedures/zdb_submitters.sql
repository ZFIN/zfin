drop trigger if exists zdb_submitters_trigger on zdb_submitters;

create or replace function zdb_submitters()
returns trigger as
$BODY$
declare name text;
begin
     
     name = (select scrub_char(NEW.name));
     NEW.name = name;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger zdb_submitters_trigger before insert or update on zdb_submitters
 for each row
 execute procedure zdb_submitters();
