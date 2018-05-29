DROP TRIGGER IF EXISTS marker_audit_trigger
ON marker;

CREATE OR REPLACE FUNCTION marker_audit()
  RETURNS trigger AS $BODY$
BEGIN
      PERFORM mhist_event(mrkr_zdb_id, '', mrkr_abbrev, '', mrkr_name);

  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;



CREATE TRIGGER marker_audit_trigger
AFTER INSERT OR UPDATE ON marker
FOR EACH ROW EXECUTE PROCEDURE marker_audit();
