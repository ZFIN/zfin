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
