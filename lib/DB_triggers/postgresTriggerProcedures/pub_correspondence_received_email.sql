DROP TRIGGER IF EXISTS pub_correspondence_received_email_trigger
ON pub_correspondence_received_email;

CREATE OR REPLACE FUNCTION pub_correspondence_received_email()
  RETURNS trigger AS $BODY$
BEGIN
  PERFORM updatePubLastCorrespondenceDate(NEW.pubcre_pub_zdb_id, NEW.pubcre_received_date);
  RETURN NULL;
END;
$BODY$ LANGUAGE plpgsql;

CREATE TRIGGER pub_correspondence_received_email_trigger
AFTER INSERT OR UPDATE ON pub_correspondence_received_email
FOR EACH ROW
EXECUTE PROCEDURE pub_correspondence_received_email();
