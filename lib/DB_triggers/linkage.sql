DROP TRIGGER IF EXISTS linkage_before_trigger
ON linkage;

CREATE OR REPLACE FUNCTION linkage_before()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.lnkg_comments = scrub_char(NEW.lnkg_comments);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER linkage_before_trigger
BEFORE INSERT OR UPDATE ON linkage
FOR EACH ROW EXECUTE PROCEDURE linkage_before();


DROP TRIGGER IF EXISTS linkage_after_trigger
ON linkage;

CREATE OR REPLACE FUNCTION linkage_after()
  RETURNS trigger AS
$BODY$
BEGIN
  PERFORM p_insert_into_record_attribution_tablezdbids(NEW.lnkg_zdb_id, NEW.lnkg_source_zdb_id);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER linkage_after_trigger
AFTER INSERT OR UPDATE ON linkage
FOR EACH ROW EXECUTE PROCEDURE linkage_after();

