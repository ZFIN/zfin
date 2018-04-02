DROP TRIGGER IF EXISTS journal_trigger
ON journal;

CREATE OR REPLACE FUNCTION journal()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.jrnl_name = scrub_char(NEW.jrnl_name);
  NEW.jrnl_abbrev = scrub_char(NEW.jrnl_abbrev);
  NEW.jrnl_name_lower = lower(NEW.jrnl_name_lower);
  NEW.jrnl_abbrev_lower = lower(NEW.jrnl_abbrev_lower);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER journal_trigger
BEFORE INSERT OR UPDATE ON journal
FOR EACH ROW EXECUTE PROCEDURE journal();
