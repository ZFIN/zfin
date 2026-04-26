-- Report: publications that were 'Closed, No PDF' but now have a file uploaded after closure
SELECT
    pth_pub_zdb_id AS "ZDB ID",
    pub.jtype AS "Pub Type",
    CASE WHEN pub.jtype = 'Journal' THEN 'Re-open' ELSE 'Closed, No data' END AS "Action",
    authors AS "Authors",
    title AS "Title",
    pts_status_display AS "Status",
    pth_status_insert_date AS "Status Changed Date",
    pf_date_entered AS "File Date",
    pft.pft_type AS "File Type",
    pf_original_file_name AS "File Name",
    'https://zfin.org/PDFLoadUp/' || pf.pf_file_name AS "File Path"
FROM
    pub_tracking_history pth
        LEFT JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
        LEFT JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
        LEFT JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
        LEFT JOIN publication_file_type pft ON pf.pf_file_type_id = pft.pft_pk_id
WHERE
    pth_status_is_current
  AND pts_status_display = 'Closed, No PDF'
  AND pf_date_entered > pth_status_insert_date
  AND NOT pub_is_indexed
ORDER BY
    pth_pub_zdb_id DESC, 2, 3, 4, 5, 6, 7, 8, 9;

-- Fix file labels: rename supplement files that were mislabeled as 'Original Article'
UPDATE publication_file
SET pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Supplemental Material')
WHERE pf_file_type_id = (SELECT pft_pk_id FROM publication_file_type WHERE pft_type = 'Original Article')
  AND lower(pf_original_file_name) LIKE '%supplement%'
  AND pf_pub_zdb_id IN (
    SELECT DISTINCT pth_pub_zdb_id
    FROM pub_tracking_history pth
             JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
             JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
             JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
    WHERE pth_status_is_current
      AND pts_status_display = 'Closed, No PDF'
      AND pf_date_entered > pth_status_insert_date
      AND NOT pub_is_indexed
);

-- Reset curation topics to blank for journal publications about to be reopened
-- (mirrors the resetCurationTopics behavior from manual reopen)
UPDATE curation
SET cur_opened_date = null,
    cur_closed_date = null,
    cur_data_found = 'f'
WHERE cur_pub_zdb_id IN (
    SELECT DISTINCT pth_pub_zdb_id
    FROM pub_tracking_history pth
             JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
             JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
             JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
    WHERE pth_status_is_current
      AND pts_status_display = 'Closed, No PDF'
      AND pf_date_entered > pth_status_insert_date
      AND NOT pub_is_indexed
      AND pub.jtype = 'Journal'
);

-- Reopen matching journal publications by setting them to 'Ready for Processing'
-- Uses 'Pub Reopen Script' identity for tracking attribution
INSERT INTO pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by, pth_status_insert_date)
SELECT DISTINCT pth_pub_zdb_id,
       (SELECT pts_pk_id FROM pub_tracking_status WHERE pts_status_display = 'Ready for Processing'),
       'ZDB-PERS-251216-1',
       now()
FROM pub_tracking_history pth
         JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
         JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
         JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
WHERE pth_status_is_current
  AND pts_status_display = 'Closed, No PDF'
  AND pf_date_entered > pth_status_insert_date
  AND NOT pub_is_indexed
  AND pub.jtype = 'Journal';

-- Transition non-journal publications to 'Closed, No data'
-- We don't curate non-journal pub types, so no re-open / no prioritization pipeline.
INSERT INTO pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by, pth_status_insert_date)
SELECT DISTINCT pth_pub_zdb_id,
       (SELECT pts_pk_id FROM pub_tracking_status WHERE pts_status_display = 'Closed, No data'),
       'ZDB-PERS-251216-1',
       now()
FROM pub_tracking_history pth
         JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
         JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
         JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
WHERE pth_status_is_current
  AND pts_status_display = 'Closed, No PDF'
  AND pf_date_entered > pth_status_insert_date
  AND NOT pub_is_indexed
  AND pub.jtype != 'Journal';

-- One-time historical cleanup: rewrite submitter on rows produced by an earlier run
-- of this script (2026-03-09) which hardcoded the developer ID instead of the
-- 'ABC-Indexing Priority Classifier' pipeline user. Idempotent — no-op after first run.
UPDATE pub_tracking_history
SET pth_status_set_by = 'ZDB-PERS-251216-1'
WHERE pth_status_set_by = 'ZDB-PERS-060413-1'
  AND pth_status_insert_date::date = '2026-03-09'
  AND pth_status_id = (SELECT pts_pk_id FROM pub_tracking_status WHERE pts_status_display = 'Ready for Processing');
