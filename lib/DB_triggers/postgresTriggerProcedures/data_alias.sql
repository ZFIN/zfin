drop trigger if exists data_alias_trigger on data_alias;

create or replace function data_alias()
returns trigger as
$BODY$

declare dalias_alias data_alias.dalias_alias%TYPE := scrub_char(NEW.dalias_alias);
declare dalias_alias_lower data_alias.dalias_alias_lower%TYPE := lower(NEW.dalias_alias);

begin

     NEW.dalias_alias = dalias_alias;
     
     NEW.dalias_alias_lower = dalias_alias_lower;
   
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger data_alias_trigger after insert or update on data_alias
 for each row
 execute procedure data_alias();
