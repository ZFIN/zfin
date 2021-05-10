begin work ;

insert into average_curation_time_metric (actm_date_captured,
						actm_average_time_in_curating_status,
						actm_stddev_time_in_curating_status,
						actm_average_time_in_bin1,
					
						actm_stddev_in_bin1,
						actm_average_time_in_bin2,
					
						actm_stddev_in_bin2,	
						actm_average_time_in_bin3,
					
						actm_stddev_in_bin3,
						actm_average_time_in_pheno,
					
						actm_stddev_in_pheno,
						actm_average_time_in_xpat,
					
						actm_stddev_in_xpat,
						actm_average_time_in_ortho,
					
						actm_stddev_in_ortho,
						actm_average_time_waiting_for_nomenclature,
						actm_stdev_time_waiting_for_nomenclature,
						actm_average_time_waiting_for_author,
						actm_stdev_time_waiting_for_author,
						actm_average_time_waiting_for_ontology,
						actm_stdev_time_waiting_for_ontology,
						actm_average_time_waiting_for_curator_review,
						actm_stdev_time_waiting_for_curator_review,
						actm_average_time_waiting_for_software_fix,
						actm_stdev_time_waiting_for_software_fix
)
select current year to second,
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'CURATING'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'CURATING'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_1'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_1'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_2'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_2'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_3'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'BIN_3'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'NEW_PHENO'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'NEW_PHENO'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'NEW_EXPR'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'NEW_EXPR'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'ORTHO'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_location
		where pth_location_id = ptl_pk_id
		and ptl_location = 'ORTHO'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'nomenclature'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'nomenclature'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'author'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'author'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'ontology'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'ontology'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'curator review'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'curator review'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select avg(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'software'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0),
	nvl((select stdev(pth_days_in_status)
		from pub_tracking_history, pub_tracking_status
		where pth_status_id = pts_pk_id
		and pts_status = 'WAIT'
		and pts_status_qualifier = 'software'
		and pth_days_in_status is not null
		and pth_status_is_current = 'f'),0)
from single;


commit work;
--rollback work;
