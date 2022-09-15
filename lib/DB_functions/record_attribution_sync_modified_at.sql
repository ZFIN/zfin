-- last modified trigger function
CREATE OR REPLACE FUNCTION record_attribution_sync_modified_at()
  RETURNS trigger AS $BODY$
BEGIN
  NEW.recattrib_modified_at := NOW();
  NEW.recattrib_modified_count := COALESCE(NEW.recattrib_modified_count, 0) + 1;
RETURN NEW;
END;
$BODY$ LANGUAGE plpgsql;

-- last modified trigger
DROP TRIGGER IF EXISTS record_attribution_sync_modified_at_trigger on record_attribution;

CREATE TRIGGER
    record_attribution_sync_modified_at_trigger
    BEFORE UPDATE ON
    record_attribution
    FOR EACH ROW EXECUTE PROCEDURE
    record_attribution_sync_modified_at();
