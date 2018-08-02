BEGIN WORK;
INSERT INTO daily_indexed_metric (dim_date_captured,
                                  dim_number_indexed_bin_1,
                                  dim_number_indexed_bin_2,
                                  dim_number_indexed_bin_3,
                                  dim_number_phenotype_bin,
                                  dim_number_expression_bin,
                                  dim_number_orthology_bin,
                                  dim_number_archived,
                                  dim_number_closed_no_data,
                                  dim_number_closed_no_pdf)
  SELECT
    now(),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'BIN_1'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'BIN_2'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'BIN_3'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'NEW_PHENO'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'NEW_EXPR'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status, pub_tracking_location
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'READY_FOR_CURATION'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
           AND ptl_location = 'ORTHO'
           AND ptl_pk_id = pth_location_id),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'CLOSED'
           AND pts_status_qualifier = 'archived'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'CLOSED'
           AND pts_status_qualifier = 'no data'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())),
    (SELECT count(*)
     FROM pub_tracking_history, pub_tracking_status
     WHERE pth_status_id = pts_pk_id
           AND pts_status = 'CLOSED'
           AND pts_status_qualifier = 'no PDF'
           AND date_trunc('day', pth_status_insert_date) = date_trunc('day', now())
    )
  FROM single;

COMMIT WORK;
