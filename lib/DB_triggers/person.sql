DROP TRIGGER IF EXISTS person_trigger
ON person;

CREATE OR REPLACE FUNCTION person()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.name = scrub_char(NEW.name);
  NEW.last_name = scrub_char(NEW.last_name);
  NEW.first_name = scrub_char(NEW.first_name);
  NEW.url = scrub_char(NEW.url);
  NEW.email = scrub_char(NEW.email);
  NEW.fax = scrub_char(NEW.fax);
  NEW.phone = scrub_char(NEW.phone);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER person_trigger
BEFORE INSERT OR UPDATE ON person
FOR EACH ROW EXECUTE PROCEDURE person();
