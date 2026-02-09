--liquibase formatted sql
--changeset cmpich:reopen-closed-no-pdf

-- Reopen publications that were 'Closed, No PDF' but now have a PDF file uploaded
-- after the status was set to closed. Sets them to 'Ready for Processing'.

INSERT INTO pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by, pth_status_insert_date)
SELECT DISTINCT pth_pub_zdb_id,
       (SELECT pts_pk_id FROM pub_tracking_status WHERE pts_status_display = 'Ready for Processing'),
       'ZDB-PERS-060413-1',
       now()
FROM pub_tracking_history pth
         JOIN pub_tracking_status pts ON pth_status_id = pts_pk_id
         JOIN publication_file pf ON pf_pub_zdb_id = pth_pub_zdb_id
         JOIN publication pub ON pth_pub_zdb_id = pub.zdb_id
WHERE pth_status_is_current
  AND pts_status_display = 'Closed, No PDF'
  AND pf_file_type_id = 1
  AND pf_date_entered > pth_status_insert_date
  AND NOT pub_is_indexed;