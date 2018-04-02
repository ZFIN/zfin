DROP TRIGGER IF EXISTS construct_trigger
ON construct;

CREATE OR REPLACE FUNCTION construct()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.construct_name = scrub_char(NEW.construct_name);
  NEW.construct_comments = scrub_char(NEW.construct_comments);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER construct_trigger
BEFORE INSERT OR UPDATE ON construct
FOR EACH ROW EXECUTE PROCEDURE construct();
