DROP TRIGGER IF EXISTS journal_before_trigger
ON journal;

CREATE OR REPLACE FUNCTION journal_before()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.jrnl_name = scrub_char(NEW.jrnl_name);
  NEW.jrnl_abbrev = scrub_char(NEW.jrnl_abbrev);
  NEW.jrnl_name_lower = lower(NEW.jrnl_name);
  NEW.jrnl_abbrev_lower = lower(NEW.jrnl_abbrev);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER journal_before_trigger
BEFORE INSERT OR UPDATE ON journal
FOR EACH ROW EXECUTE PROCEDURE journal_before();


DROP TRIGGER IF EXISTS journal_before_after
ON journal;

CREATE OR REPLACE FUNCTION journal_after()
  RETURNS trigger AS
$BODY$
BEGIN
  PERFORM addsourcealias(OLD.jrnl_zdb_id, OLD.jrnl_name);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER journal_before_after
AFTER UPDATE ON journal
FOR EACH ROW EXECUTE PROCEDURE journal_after();
