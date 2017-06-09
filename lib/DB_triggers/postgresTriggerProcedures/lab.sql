drop trigger if exists lab_trigger on lab;

create or replace function lab()
returns trigger as
$BODY$

declare phone lab.phone%TYPE;
declare fax lab.fax%TYPE;
declare email lab.email%TYPE;
declare url lab.url%TYPE;
declare name lab.name%TYPE;

begin

     phone = (select scrub_char(NEW.phone));
     NEW.phone = phone;

     fax = (select scrub_char(NEW.fax));
     NEW.fax = fax;

     url = (select scrub_char(NEW.url));
     NEW.url = url;

     name = (Select scrub_char(NEW.name));
     NEW.name = name;

     email = (select scrub_char(NEW.email));
     NEW.email = email;
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger lab_trigger before insert or update on lab
 for each row
 execute procedure lab();
