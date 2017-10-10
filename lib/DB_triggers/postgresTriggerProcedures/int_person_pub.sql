drop trigger if exists int_person_pub_trigger on int_person_pub;

create or replace function int_person_pub()
returns trigger as
$BODY$
declare info int_person_pub.info%TYPE := scrub_char(NEW.info);
declare flag int_person_pub.flag%TYPE := scrub_char(NEW.flag);
declare status int_person_pub.status%TYPE := scrub_char(NEW.status);

begin
     
     NEW.info = info;

     NEW.flag = flag;
 
     NEW.status = status;


     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger int_person_pub_trigger after insert or update on int_person_pub
 for each row
 execute procedure int_person_pub();
