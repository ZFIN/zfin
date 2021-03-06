
create temp table tmp_id (id TIMESTAMP);

insert into tmp_id (id)
	select CURRENT_TIMESTAMP;

--date(CURRENT_TIMESTAMP)-date(pth_status_insert_date)) = days in current status
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
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_1'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't') ,
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_2'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_3'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'NEW_PHENO'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'NEW_EXPR'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(avg(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'ORTHO'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_1'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_2'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'BIN_3'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'NEW_PHENO'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'NEW_EXPR'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't'),
		(select nvl( round(max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))::numeric,0),0)
		 from pub_tracking_history, pub_tracking_location, pub_tracking_status
		 where pth_location_id = ptl_pk_id
					 and ptl_location = 'ORTHO'
					 and pts_pk_id = pth_status_id
					 and pts_status = 'READY_FOR_CURATION'
					 and pth_status_is_current = 't')
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
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'BIN_1'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't');

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
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'BIN_2'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't');

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
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'BIN_3'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't' );



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
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'NEW_PHENO'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't');

insert into longest_bin_resident_metric (lbrm_date_captured,
																				 lbrm_pub_zdb_id,
																				 lbrm_status_counted,
																				 lbrm_days_in_status_on_this_date)
	select distinct id, pth_pub_zdb_id, 'NEW_EXPR',(date(id)-date(pth_status_insert_date))
	from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
	where pth_location_id = ptl_pk_id
				and ptl_location = 'NEW_EXPR'
				and pts_pk_id = pth_status_id
				and pts_status = 'READY_FOR_CURATION'
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'NEW_EXPR'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't');

insert into longest_bin_resident_metric (lbrm_date_captured,
																				 lbrm_pub_zdb_id,
																				 lbrm_status_counted,
																				 lbrm_days_in_status_on_this_date)
	select distinct id, pth_pub_zdb_id, 'ORTHO',(date(id)-date(pth_status_insert_date))
	from pub_tracking_history, pub_tracking_location, pub_tracking_status, publication, tmp_id
	where pth_location_id = ptl_pk_id
				and ptl_location = 'ORTHO'
				and pts_pk_id = pth_status_id
				and pts_status = 'READY_FOR_CURATION'
				and pth_status_is_current = 't'
				and date(CURRENT_TIMESTAMP)-date(pth_status_insert_date) = (select max(date(CURRENT_TIMESTAMP)-date(pth_status_insert_date))
																																				 from pub_tracking_history, pub_tracking_location, pub_tracking_status
																																				 where pth_location_id = ptl_pk_id
																																							 and ptl_location = 'ORTHO'
																																							 and pts_pk_id = pth_status_id
																																							 and pts_status = 'READY_FOR_CURATION'
																																							 and pth_status_is_current = 't');


unload to metrics
 select macm_date_captured,
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
  from monthly_average_curated_metric
 where macm_date_captured = (select max(macm_date_Captured)
       			      from monthly_average_curated_metric)
;
       

unload to pubs-with-longest-days-in-status
 select lbrm_date_captured,
       	    			         lbrm_pub_zdb_id,
					 lbrm_status_counted,
					 lbrm_days_in_status_on_this_date
   from longest_bin_resident_metric

   	where lbrm_date_captured = (select max(lbrm_date_Captured)
       			      from longest_bin_resident_metric)
  order by lbrm_status_counted ;
