CREATE TEMP TABLE tmp_results AS
  SELECT
    accession_no,
    zdb_id,
    ptl_location_display,
    jrnl_name,
    title,
    min(pth_status_insert_date)
  FROM publication, pub_tracking_status, pub_tracking_location, pub_tracking_history, journal
  WHERE pub_jrnl_zdb_id = jrnl_zdb_id
        AND ptl_pk_id = pth_location_id
        AND pts_pk_id = pth_status_id
        AND pth_pub_zdb_id = zdb_id
        AND pts_status_display = 'Ready for Curation'
        AND ptl_location_display NOT IN ('1', '2', '3')
  GROUP BY accession_no, zdb_id, ptl_location_display, jrnl_name, title;

\COPY tmp_results TO './pubTrackingReport.txt';
