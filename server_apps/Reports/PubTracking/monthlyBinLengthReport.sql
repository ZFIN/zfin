begin work;

insert into monthly_curated_metric (mcm_pub_arrival_date_month,
       	    			  mcm_pub_arrival_date_year)
select distinct month(pub_arrival_date), year(pub_arrival_date)
  from publication
 where exists (Select 'x' from pub_tracking_history, pub_tracking_status
       	      	      where pth_pub_zdb_id = zdb_id
		      and pth_status_id = pts_pk_id
		      and pts_status = 'READY_FOR_CURATION');


update monthly_curated_metric
  set mcm_number_in_bin_1 = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_1'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_in_bin_2 = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_2'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_in_bin_3 = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_3'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_in_phenotype_bin = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_PHENO'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_in_expression_bin = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_XPAT'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);


update monthly_curated_metric
  set mcm_number_in_ortho_bin = (Select count(*) from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_ORTHO'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_archived_this_month = (Select count(*) from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier = 'Archived'
					     and year(pth_status_insert_date) = year(current year to month)
					     and month(pth_status_insert_date) = month(current year to month)
					     and pth_status_is_current = 't'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);

update monthly_curated_metric
  set mcm_number_closed_unread_this_month = (Select count(*) from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier in ('not a zebrafish paper','no data', 'no PDF')
					     and year(pth_status_insert_date) = year(current year to month)
					     and month(pth_status_insert_date) = month(current year to month)
					     and pth_status_is_current = 't'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);


update monthly_curated_metric
  set mcm_number_closed_curated_this_month = (Select count(*) from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier in ('curated')
					     and year(pth_status_insert_date) = year(current year to month)
					     and month(pth_status_insert_date) = month(current year to month)
					     and pth_status_is_current = 't'
					     and month(pub_arrival_date) = mcm_pub_arrival_date_month
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year);


select first 10 * from monthly_Curated_metric;
commit work;
