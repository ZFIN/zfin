--liquibase formatted sql
--changeset pkalita:14538updateExtNotes

UPDATE external_note
SET extnote_source_zdb_id = (
  SELECT tmp_pub_id
  FROM tmp_feature_pubs
  WHERE tmp_feature_id = extnote_data_zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_feature_pubs
  WHERE tmp_feature_id = extnote_data_zdb_id
);

DROP TABLE tmp_feature_pubs;