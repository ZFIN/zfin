drop trigger if exists construct_trigger on construct;

create or replace function construct()
returns trigger as
$BODY$
declare construct_name construct.construct_name%TYPE := scrub_char(NEW.construct_name;

begin

     NEW.construct_name = construct_name;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger construct_trigger after insert or update on construct
 for each row
 execute procedure construct();
