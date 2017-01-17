
create temp table tmp_id (id datetime year to second)
 with no log;

insert into tmp_id (id)
 select current year to second from single;

--date(current year to second)-date(pth_status_insert_date)) = days in current status
insert into monthly_average_curated_metric (macm_date_captured,
       	    				    macm_average_stay_in_bin_1,	
       	    				    macm_average_stay_in_bin_2,	
					    macm_average_stay_in_bin_3,	
					    macm_average_stay_in_pheno_bin,	
					    macm_average_stay_in_xpat_bin,	
					    macm_average_stay_in_ortho_bin,
					    macm_longest_bin1_number_of_days,
					    macm_longest_bin2_number_of_days,
					    macm_longest_bin3_number_of_days,
					    macm_longest_phenotype_number_of_days,
					    macm_longest_expression_number_of_days,
					    macm_longest_ortho_number_of_days
)					    
	select id,
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_1'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION') ,
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_2'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_3'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_PHENO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_XPAT'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
	(select nvl( avg(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_ORTHO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_1'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_2'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_3'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_PHENO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_XPAT'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'),
        (select nvl( max(date(current year to second)-date(pth_status_insert_date)),0)
		from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_ORTHO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION')
 from tmp_id;

select * from monthly_average_curated_metric;


insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'BIN_1',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_1'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'BIN_1'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');

insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'BIN_2',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_2'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION' 
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'BIN_2'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');

insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'BIN_3',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'BIN_3'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'BIN_3'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');



	insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'NEW_PHENO',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_PHENO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'NEW_PHENO'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');

	insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'NEW_XPAT',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_XPAT'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'NEW_XPAT'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');

insert into longest_bin_resident_metric (lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date)
 select distinct id, pth_pub_zdb_id, 'NEW_ORTHO',(date(id)-date(pth_status_insert_date))
		from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
		     where pth_location_id = ptl_pk_id
		     and ptl_location = 'NEW_ORTHO'
		     and pts_pk_id = pth_status_id
		     and pts_status = 'READY_FOR_CURATION'
		     and date(current year to second)-date(pth_status_insert_date) = (select max(date(current year to second)-date(pth_status_insert_date))
		     	 					  	  from pub_tracking_history, pub_tracking_location, pub_tracking_status
		     							  where pth_location_id = ptl_pk_id
		     							  and ptl_location = 'NEW_ORTHO'
		     							  and pts_pk_id = pth_status_id
		     							  and pts_status = 'READY_FOR_CURATION');			 


