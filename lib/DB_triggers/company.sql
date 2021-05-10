DROP TRIGGER IF EXISTS company_trigger
ON company;

CREATE OR REPLACE FUNCTION company()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.phone = scrub_char(NEW.phone);
  NEW.fax = scrub_char(NEW.fax);
  NEW.url = scrub_char(NEW.url);
  NEW.name = scrub_char(NEW.name);
  NEW.email = scrub_char(NEW.email);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER company_trigger
BEFORE INSERT OR UPDATE ON company
FOR EACH ROW EXECUTE PROCEDURE company();
