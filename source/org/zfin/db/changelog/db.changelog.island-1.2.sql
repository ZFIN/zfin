--liquibase formatted sql

--changeset pkalita:case-13613

DROP FUNCTION IF EXISTS setpubindexeddate;
DROP TRIGGER IF EXISTS publication_is_indexed_update_trigger;

CREATE TABLE tmp_dates (
  pub_zdb_id VARCHAR(50),
  index_date DATETIME YEAR TO SECOND
);

INSERT INTO tmp_dates
SELECT publication_note.pnote_pub_zdb_id, MAX(TRUNC(publication_note.pnote_date, 'DD'))
FROM publication_note
INNER JOIN publication ON publication_note.pnote_pub_zdb_id = publication.zdb_id
WHERE publication_note.pnote_text = 'Indexed paper'
AND TRUNC(publication_note.pnote_date, 'DD') != publication.pub_indexed_date
GROUP BY publication_note.pnote_pub_zdb_id;

UPDATE publication
SET publication.pub_indexed_date = (
  SELECT index_date
  FROM tmp_dates
  WHERE publication.zdb_id = tmp_dates.pub_zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_dates
  WHERE publication.zdb_id = tmp_dates.pub_zdb_id
);

drop table tmp_dates;