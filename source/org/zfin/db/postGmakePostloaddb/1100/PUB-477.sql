-- make a place to store who indexed the pub on the publication table
ALTER TABLE publication
  ADD COLUMN pub_indexed_by text
  CONSTRAINT pub_indexer_foreign_key
  REFERENCES person (zdb_id)
  ON UPDATE RESTRICT
  ON DELETE RESTRICT;

-- if there was an indexed status in pub_tracking_history make
-- sure the publication table reflects that
UPDATE publication
SET (pub_is_indexed, pub_indexed_date, pub_indexed_by) = (
  SELECT true, pth_status_insert_date, pth_status_set_by
  FROM pub_tracking_history
  WHERE pth_status_id = 16 -- INDEXED
  AND pth_pub_zdb_id = zdb_id
  ORDER BY pth_status_insert_date DESC
  LIMIT 1
) WHERE EXISTS(
  SELECT 'x'
  FROM pub_tracking_history
  WHERE pth_status_id = 16
  AND pth_pub_zdb_id = zdb_id
);

-- remove indexed status entries from pub_tracking_history
DELETE FROM pub_tracking_history
WHERE pth_status_id = 16;

-- remove indexed status itself
DELETE FROM pub_tracking_status
WHERE pts_pk_id = 16;
