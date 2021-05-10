DROP TRIGGER IF EXISTS clone_before_trigger
ON clone;

DROP TRIGGER IF EXISTS clone_after_trigger
ON clone;

CREATE OR REPLACE FUNCTION clone_before()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.clone_pcr_amplification = scrub_char(NEW.clone_pcr_amplification);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION clone_after()
  RETURNS trigger AS $BODY$
BEGIN
  PERFORM p_update_clone_relationship(NEW.clone_mrkr_zdb_id, NEW.clone_problem_type);
  RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER clone_before_trigger
BEFORE INSERT OR UPDATE ON clone
FOR EACH ROW EXECUTE PROCEDURE clone_before();

CREATE TRIGGER clone_after_trigger
AFTER INSERT OR UPDATE ON clone
FOR EACH ROW EXECUTE PROCEDURE clone_after();
