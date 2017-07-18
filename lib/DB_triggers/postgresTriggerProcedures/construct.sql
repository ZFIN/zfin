drop trigger if exists construct_trigger on construct;

create or replace function construct()
returns trigger as
$BODY$
declare construct_name construct.construct_name%TYPE;

begin

     construct_name = (select scrub_char(NEW.construct_name));
     NEW.construct_name = construct_name;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger construct_trigger before insert or update on construct
 for each row
 execute procedure construct();
