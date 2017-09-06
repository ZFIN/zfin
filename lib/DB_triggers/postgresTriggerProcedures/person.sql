drop trigger if exists person_trigger on person;

create or replace function person()
returns trigger as
$BODY$
declare url text := scrub_char(NEW.url);
        email text := scrub_char(NEW.email);
	fax   text := scrub_char(NEW.fax);
	phone text := scrub_char(NEW.phone);
begin

	NEW.url = url;

	NEW.email = email;

	NEW.fax = fax;

	NEW.phone = phone;	

	RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger person_trigger before insert or update on person
 for each row
 execute procedure person();
