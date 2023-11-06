--liquibase formatted sql
--changeset rtaylor:ZFIN-8696-03-post.sql

SELECT
    marker_id,
    symbol,
    count(*),
    row_number() OVER (ORDER BY marker_id) as rownum,
    string_agg('UniProt has determined that the Ensembl ID (' || ensdarg || ') associated with the feature belongs to ' || accession || '.', '
') AS note
INTO temp table notes_8696
FROM
    temp_8696
GROUP BY
    marker_id,
    symbol
ORDER BY
    marker_id;

INSERT INTO zdb_active_data SELECT 'ZDB-DNOTE-231020-' || rownum from notes_8696;
INSERT INTO data_note (dnote_zdb_id, dnote_curator_zdb_id, dnote_data_zdb_id, dnote_date, dnote_text)
  SELECT 'ZDB-DNOTE-231020-' || rownum, 'ZDB-PERS-210917-1', marker_id, now(), note FROM notes_8696;
