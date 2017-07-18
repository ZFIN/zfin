drop trigger if exists person_trigger on person;

create or replace function person()
returns trigger as
$BODY$
declare url text;
        email text;
	fax   text;
	phone text;
begin

	url = (select scrub_char(NEW.url));
	NEW.url = url;

	email = (select scrub_char(NEW.email));
	NEW.email = email;

	fax = (select scrub_char(NEW.fax));
	NEW.fax = fax;

	phone = (select scrub_char(NEW.phone));
	NEW.phone = phone;	

	RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger person_trigger before insert or update on person
 for each row
 execute procedure person();
