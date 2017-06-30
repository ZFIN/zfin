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
       
