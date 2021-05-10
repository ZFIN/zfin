insert into monthly_curated_metric (mcm_pub_arrival_date_month,
                                    mcm_pub_arrival_date_year)
  select distinct month(pub_arrival_date), year(pub_arrival_date)
  from publication
  where exists (Select 'x' from pub_tracking_history, pub_tracking_status
  where pth_pub_zdb_id = zdb_id
        and pth_status_id = pts_pk_id
        and pts_status = 'READY_FOR_CURATION'
        and pth_status_is_current = 't');



CREATE TEMP TABLE bin1 AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'BIN_1'
      and pth_status_is_current = 't'
group by month, year;

CREATE TEMP TABLE bin2 AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'BIN_2'
      and pth_status_is_current = 't'
group by month, year;


CREATE TEMP TABLE bin3 AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'BIN_3'
      and pth_status_is_current = 't'
group by month, year;

CREATE TEMP TABLE newpheno AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'NEW_PHENO'
      and pth_status_is_current = 't'
group by month, year;

CREATE TEMP TABLE newortho AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'ORTHO'
      and pth_status_is_current = 't'
group by month, year;


CREATE TEMP TABLE newxpat AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history,
  pub_tracking_location,
  publication
where pth_location_id = ptl_pk_id
      and pth_pub_Zdb_id = zdb_id
      and ptl_location = 'NEW_EXPR'
      and pth_status_is_current = 't'
group by month, year;

CREATE TEMP TABLE closedArchived AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history, pub_tracking_status,	 publication
where pth_pub_Zdb_id = zdb_id
      and pts_pk_id = pth_status_id
      and pts_status = 'CLOSED'
      and pts_status_qualifier in( 'archived')
      and pth_status_is_current = 't'
group by month, year;

CREATE TEMP TABLE closednotazebrafishpaper AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history, pub_tracking_status,	 publication
where pth_pub_Zdb_id = zdb_id
      and pts_pk_id = pth_status_id
      and pts_status = 'CLOSED'
      and pts_status_qualifier in( 'not a zebrafish paper')
      and pth_status_is_current = 't'
group by month, year;




CREATE TEMP TABLE closedCurated AS
select count(*) as counter, month(pub_arrival_date) as month, year(pub_arrival_date) as year
from pub_tracking_history, pub_tracking_status,	 publication
where pth_pub_Zdb_id = zdb_id
      and pts_pk_id = pth_status_id
      and pts_status = 'CLOSED'
      and pts_status_qualifier = 'curated'
      and pth_status_is_current = 't'
group by month, year;

update monthly_curated_metric
set mcm_number_in_bin_1 = nvl((select counter from bin1
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);


update monthly_curated_metric
set mcm_number_in_bin_2 = nvl((select counter from bin2
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);

update monthly_curated_metric
set mcm_number_in_bin_3 = nvl((select counter from bin3
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);

update monthly_curated_metric
set mcm_number_in_phenotype_bin = nvl((select counter from newpheno
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);

update monthly_curated_metric
set mcm_number_in_expression_bin = nvl((select counter from newxpat
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);
update monthly_curated_metric
set mcm_number_in_ortho_bin = nvl((select counter from newortho
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);

update monthly_curated_metric
set mcm_number_archived_this_month = nvl((select counter from closedArchived
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);


update monthly_curated_metric
set mcm_number_closed_unread_this_month = nvl((select counter from closednotazebrafishpaper
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);
update monthly_curated_metric
set mcm_number_closed_curated_this_month = nvl((select counter from closedCurated
where mcm_pub_arrival_date_month = month
      and mcm_pub_arrival_date_year = year),0);



select * from monthly_Curated_metric limit 10;

select mcm_date_Captured,
       LPAD(mcm_pub_arrival_date_month, 2, '0') as month,
       mcm_pub_arrival_date_year,
       mcm_number_in_bin_1,
       mcm_number_in_bin_2,
       mcm_number_in_bin_3,
       mcm_number_in_phenotype_bin,
       mcm_number_in_expression_bin,
       mcm_number_in_ortho_bin,
       mcm_number_closed_unread_this_month,
       mcm_number_archived_this_month,
       mcm_number_closed_Curated_this_month
 from monthly_curated_metric
 where mcm_date_captured = (select max(mcm_date_Captured)
       			      from monthly_curated_metric)
 order by mcm_pub_arrival_date_year, month asc;
       
