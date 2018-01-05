unload to pubTrackingReport.txt
select accession_no, zdb_id, ptl_location_display, jrnl_name, title, min(pth_status_insert_date)
  from publication, pub_tracking_status, pub_tracking_location, pub_tracking_history, journal
 where pub_jrnl_zdb_id = jrnl_zdb_id
 and ptl_pk_id = pth_location_id
and pts_pk_id = pth_status_id
and pth_pub_zdb_id = zdb_id
and pts_status_display = 'Ready for Curation'
and ptl_location_display not in ('1','2','3')
 group by accession_no, zdb_id, ptl_location_display, jrnl_name, title
;
