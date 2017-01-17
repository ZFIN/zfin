select actm_date_captured,
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
 from average_curation_time_metric
 where actm_date_captured = (select max(actm_date_Captured)
       			      from average_curation_time_metric)
;
       
