drop trigger if exists source_alias_trigger on source_alias;

create or replace function source_alias()
returns trigger as
$BODY$
declare salias_alias text;
declare salias_alias_lower text;
begin

     salias_alias = (select scrub_char(NEW.salias_alias));
     NEW.salias_alias = salias_alias;

     salias_alias_lower = lower(NEW.salias_alias_lower);
     NEW.salias_alias_lower = salias_alias_lower;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger source_alias_trigger before insert or update on source_alias
 for each row
 execute procedure source_alias();
