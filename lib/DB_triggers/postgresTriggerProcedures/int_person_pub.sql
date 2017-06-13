drop trigger if exists int_person_pub_trigger on int_person_pub;

create or replace function int_person_pub()
returns trigger as
$BODY$
declare info int_person_pub.info%TYPE;
declare flag int_person_pub.flag%TYPE;
declare status int_person_pub.status%TYPE;

begin
     
     info = (select scrub_char(NEW.info));
     NEW.info = info;

     flag = (select scrub_char(NEW.flag));
     NEW.flag = flag;
 
     status = (select scrub_char(NEW.status));
     NEW.status = status;


     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger int_person_pub_trigger before insert or update on int_person_pub
 for each row
 execute procedure int_person_pub();
