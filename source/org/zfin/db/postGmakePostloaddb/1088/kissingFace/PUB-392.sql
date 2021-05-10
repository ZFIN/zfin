--liquibase formatted sql
--changeset pkalita:PUB-392

CREATE TEMP TABLE tmp_mini_refs (
  tmp_pub_id VARCHAR(50),
  tmp_mini_ref VARCHAR(100)
);

INSERT INTO tmp_mini_refs
SELECT zdb_id, get_pub_mini_ref(zdb_id)
FROM publication
WHERE pub_mini_ref IS NULL;

UPDATE publication
SET pub_mini_ref = (
  SELECT tmp_mini_ref
  FROM tmp_mini_refs
  WHERE tmp_pub_id = zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_mini_refs
  WHERE tmp_pub_id = zdb_id
);

DROP TABLE tmp_mini_refs;
