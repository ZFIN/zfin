--liquibase formatted sql
--changeset pkalita:14815

create temp table tmp_closed_dates (pub_zdb_id varchar(50), closed_date datetime year to second) with no log;

INSERT INTO tmp_closed_dates
SELECT zdb_id, pth_status_insert_date
FROM publication
INNER JOIN pub_tracking_history on zdb_id = pth_pub_zdb_id
INNER JOIN pub_tracking_status on pth_status_id = pts_pk_id
WHERE pub_completion_date IS NULL
AND pts_status_display = 'Closed, Curated'
AND pth_status_is_current = 't';

UPDATE publication
SET pub_completion_date = (
  SELECT closed_date
  FROM tmp_closed_dates
  WHERE pub_zdb_id = zdb_id
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_closed_dates
  WHERE pub_zdb_id = zdb_id
);

DROP TABLE tmp_closed_dates;
