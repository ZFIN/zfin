begin work;

insert into monthly_curated_metric (mcm_pub_arrival_date_month,
       	    			  mcm_pub_arrival_date_year)
select distinct month(pub_arrival_date), year(pub_arrival_date)
  from publication
 where exists (Select 'x' from pub_tracking_history, pub_tracking_status
       	      	      where pth_pub_zdb_id = zdb_id
		      and pth_status_id = pts_pk_id
		      and pts_status = 'READY_FOR_CURATION'
		      and pth_status_is_current = 't');


select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_1'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp bin1;

select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_2'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp bin2;


select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'BIN_3'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp bin3;


select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_PHENO'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp newpheno;

select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_ORTHO'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp newortho;


select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
         from pub_tracking_history, 
      			    	    	     pub_tracking_location,
					     publication
					     where pth_location_id = ptl_pk_id
					     and pth_pub_Zdb_id = zdb_id
					     and ptl_location = 'NEW_XPAT'
					     and pth_status_is_current = 't'
	 group by month, year
 into temp newxpat;

select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
        from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier = 'Archived'
					     and pth_status_is_current = 't'
	group by month, year
into temp closedArchived;

select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
        from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier = 'not a zebrafish paper'
					     and pth_status_is_current = 't'
	group by month, year
into temp closedNotZebrafish;


select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
        from pub_tracking_history, pub_tracking_status,	 publication
					     where pth_pub_Zdb_id = zdb_id
					     and pts_pk_id = pth_status_id
					     and pts_status = 'CLOSED'
					     and pts_status_qualifier = 'curated'
					     and pth_status_is_current = 't'
	group by month, year
into temp closedCurated;





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
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year
					     and pth_status_is_current = 't');


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
					     and year(pub_arrival_date) = mcm_pub_arrival_date_year
					     and pth_status_is_current = 't');


select first 10 * from monthly_Curated_metric;
commit work;
