insert into daily_indexed_metric (dim_date_captured,
                                  dim_number_indexed_bin_1,
                                  dim_number_indexed_bin_2,
                                  dim_number_indexed_bin_3,
                                  dim_number_phenotype_bin,
                                  dim_number_expression_bin,
                                  dim_number_orthology_bin,
                                  dim_number_archived,
                                  dim_number_closed_no_data,
                                  dim_number_closed_no_pdf)
       select now(), (select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'BIN_1'
and ptl_pk_id = pth_location_id ),
(select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'BIN_2'
and ptl_pk_id = pth_location_id ),
(select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'BIN_3'
and ptl_pk_id = pth_location_id ),
(select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'NEW_PHENO'
and ptl_pk_id = pth_location_id ),
(select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'NEW_EXPR'
and ptl_pk_id = pth_location_id ),
(select count(*) from pub_tracking_history, pub_tracking_status, pub_tracking_location
where pth_status_id = pts_pk_id
and pts_status = 'READY_FOR_CURATION'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
and ptl_location = 'ORTHO'
and ptl_pk_id = pth_location_id ),

(select count(*) from pub_tracking_history, pub_tracking_status
where pth_status_id = pts_pk_id
and pts_status = 'CLOSED'
and pts_status_qualifier = 'archived'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)),
(select count(*) from pub_tracking_history, pub_tracking_status
where pth_status_id = pts_pk_id
and pts_status = 'CLOSED'
and pts_status_qualifier = 'no data'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)),
(select count(*) from pub_tracking_history, pub_tracking_status
where pth_status_id = pts_pk_id
and pts_status = 'CLOSED'
and pts_status_qualifier = 'no PDF'
and day(pth_status_insert_date) = day(current_date)
and year(pth_status_insert_date)::numeric = year(current_date)::numeric
and month(pth_status_insert_date) = month(current_date)
);


select dim_date_captured,
       dim_number_indexed_bin_1,	
       dim_number_indexed_bin_2,
       dim_number_indexed_bin_3, 
       dim_number_archived,
       dim_number_phenotype_bin,
       dim_number_orthology_bin,
       dim_number_expression_bin,
       dim_number_closed_no_data,
       dim_number_closed_no_pdf
 from daily_indexed_metric
 where dim_date_captured =  (select max(dim_date_captured)
       			       from daily_indexed_metric
			       	);
