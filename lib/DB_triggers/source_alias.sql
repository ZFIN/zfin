DROP TRIGGER IF EXISTS source_alias_trigger
ON source_alias;

CREATE OR REPLACE FUNCTION source_alias()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.salias_alias = scrub_char(NEW.salias_alias);
  NEW.salias_alias_lower = lower(NEW.salias_alias);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER source_alias_trigger
BEFORE INSERT OR UPDATE ON source_alias
FOR EACH ROW
EXECUTE PROCEDURE source_alias();
