DROP TRIGGER IF EXISTS pub_correspondence_sent_tracker_trigger
ON pub_correspondence_sent_tracker;

CREATE OR REPLACE FUNCTION pub_correspondence_sent_tracker()
  RETURNS trigger AS $BODY$
BEGIN
  PERFORM updatePubLastCorrespondenceDate(NEW.pubcst_pub_zdb_id, NEW.pubcst_date_sent);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER pub_correspondence_sent_tracker_trigger
AFTER INSERT OR UPDATE ON pub_correspondence_sent_tracker
FOR EACH ROW
EXECUTE PROCEDURE pub_correspondence_sent_tracker();
