drop trigger if exists source_alias_trigger on source_alias;

create or replace function source_alias()
returns trigger as
$BODY$
declare salias_alias text := scrub_char(NEW.salias_alias);
declare salias_alias_lower text := lower(NEW.salias_alias_lower);
begin

     NEW.salias_alias = salias_alias;

     NEW.salias_alias_lower = salias_alias_lower;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger source_alias_trigger after insert or update on source_alias
 for each row
 execute procedure source_alias();
