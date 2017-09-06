drop trigger if exists lab_trigger on lab;

create or replace function lab()
returns trigger as
$BODY$

declare phone lab.phone%TYPE := scrub_char(NEW.phone);
declare fax lab.fax%TYPE := scrub_char(NEW.fax);
declare email lab.email%TYPE := scrub_char(NEW.email);
declare url lab.url%TYPE := scrub_char(NEW.url);
declare name lab.name%TYPE :=scrub_char(NEW.name);

begin

     NEW.phone = phone;

     NEW.fax = fax;

     NEW.url = url;

     NEW.name = name;

     NEW.email = email;
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger lab_trigger before insert or update on lab
 for each row
 execute procedure lab();
