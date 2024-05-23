SELECT
    pth_pub_zdb_id AS "ZDB ID",
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
  AND pts_status = 'CLOSED'
  AND pts_status_display <> 'Closed, Curated'
  AND pf_file_type_id = 1
  AND pf_date_entered > pth_status_insert_date
  AND NOT pub_is_indexed
ORDER BY
    pth_status_insert_date DESC