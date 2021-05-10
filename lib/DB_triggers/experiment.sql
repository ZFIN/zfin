DROP TRIGGER IF EXISTS experiment_before_trigger
ON experiment;

DROP TRIGGER IF EXISTS experiment_after_trigger
ON experiment;

CREATE OR REPLACE FUNCTION experiment_before()
  RETURNS trigger AS
$BODY$
BEGIN
  NEW.exp_name = scrub_char(NEW.exp_name);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION experiment_after()
  RETURNS trigger AS
$BODY$
BEGIN
  PERFORM p_insert_into_record_attribution_tablezdbids(
      NEW.exp_zdb_id,
      NEW.exp_source_zdb_id);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER experiment_before_trigger
BEFORE INSERT OR UPDATE ON experiment
FOR EACH ROW EXECUTE PROCEDURE experiment_before();

CREATE TRIGGER experiment_after_trigger
AFTER INSERT OR UPDATE ON experiment
FOR EACH ROW EXECUTE PROCEDURE experiment_after();
