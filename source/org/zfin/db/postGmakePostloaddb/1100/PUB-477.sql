-- if there was an indexed status in pub_tracking_history make
-- sure the publication table reflects that
UPDATE publication
SET
  pub_is_indexed = 't',
  pub_indexed_date = (
    SELECT pth_status_insert_date
    FROM pub_tracking_history
    WHERE pth_status_id = 16 -- INDEXED
    AND pth_pub_zdb_id = zdb_id
  )
WHERE EXISTS(
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
