DROP TRIGGER IF EXISTS marker_audit_insert_trigger
ON marker;

DROP TRIGGER IF EXISTS marker_audit_update_trigger
ON marker;

CREATE OR REPLACE FUNCTION marker_audit_insert()
  RETURNS trigger AS $BODY$
BEGIN
      PERFORM mhist_event(NEW.mrkr_zdb_id, '', NEW.mrkr_name, '', NEW.mrkr_abbrev);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION marker_audit_update()
  RETURNS trigger AS $BODY$
BEGIN
      PERFORM mhist_event(NEW.mrkr_zdb_id, OLD.mrkr_name, NEW.mrkr_name, OLD.mrkr_abbrev, NEW.mrkr_abbrev);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;


CREATE TRIGGER marker_audit_insert_trigger
AFTER INSERT on marker
FOR EACH ROW EXECUTE PROCEDURE marker_audit();


CREATE TRIGGER marker_audit_update_trigger
AFTER UPDATE on marker
FOR EACH ROW EXECUTE PROCEDURE marker_audit();
