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
