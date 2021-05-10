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

-- mark pubs that went directly from Indexing to Closed, Waiting, or Curating
-- that aren't currently on Holly's dashboard as indexed.
UPDATE publication update_pub
SET pub_is_indexed = TRUE,
  pub_indexed_by   = 'ZDB-PERS-100329-1',
  pub_indexed_date = sub.indexed_date
FROM (
       SELECT
         pub.zdb_id                                AS pub_zdb_id,
         indexing.pth_status_made_non_current_date AS indexed_date
       FROM publication pub
         INNER JOIN pub_tracking_history indexing ON pub.zdb_id = indexing.pth_pub_zdb_id
         CROSS JOIN pub_tracking_history NEXT
         INNER JOIN pub_tracking_status indexing_status ON indexing.pth_status_id = indexing_status.pts_pk_id
         INNER JOIN pub_tracking_status next_status ON NEXT.pth_status_id = next_status.pts_pk_id
         INNER JOIN pub_tracking_history CURRENT ON pub.zdb_id = CURRENT.pth_pub_zdb_id
       WHERE indexing_status.pts_status = 'INDEXING'
             AND next_status.pts_status IN ('WAIT', 'CLOSED', 'CURATING')
             AND indexing.pth_pub_zdb_id = NEXT.pth_pub_zdb_id
             AND NEXT.pth_status_insert_date > indexing.pth_status_insert_date
             AND EXTRACT('epoch' FROM (NEXT.pth_status_insert_date - indexing.pth_status_made_non_current_date)) < 90
             AND pub.pub_is_indexed = 'f'
             AND CURRENT.pth_status_is_current = 't'
             AND (CURRENT.pth_claimed_by IS NULL OR CURRENT.pth_claimed_by != 'ZDB-PERS-100329-1')
     ) AS sub
WHERE update_pub.zdb_id = sub.pub_zdb_id;
