--liquibase formatted sql
--changeset sierra:createStatsTables

create table daily_indexed_metric (dim_pk_id serial8 not null constraint dim_pk_id_not_null,
       	     			    dim_number_indexed_bin_1 int default 0 not null constraint dim_number_indexed_bin_1_not_null,
				    dim_number_indexed_bin_2 int default 0 not null constraint dim_number_indexed_bin_2_not_null,
				    dim_number_indexed_bin_3 int default 0 not null constraint dim_number_indexed_bin_3_not_null, 
				    dim_number_archived int default 0 not null constraint dim_number_archived_not_null,
				    dim_number_phenotype_bin int default 0 not null constraint dim_number_phenotype_bin_not_null,
				    dim_number_expression_bin int default 0 not null constraint dim_number_expression_bin_not_null,
				    dim_number_closed_no_data int default 0 not null constraint dim_number_closed_no_data_not_null,
				    dim_date_captured datetime year to second default current year to second not null constraint dim_date_captured_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create unique index daily_indexed_metric_pk_index
  on daily_indexed_metric (dim_pk_id)
 using btree in idxdbs3;

alter table daily_indexed_metric
 add constraint primary key (dim_pk_id)
 constraint daily_indexed_metric_primary_key;



create table monthly_curated_metric (mcm_pk_id serial8 not null constraint mcm_pk_id_not_null,
       	     			      mcm_number_in_bin_1 int default 0 not null constraint mcm_number_in_bin_1_not_null,
				      mcm_paper_in_bin_1_longest varchar(50),
				      mcm_longest_bin1_number_of_days int default 0 not null constraint mcm_longest_bin1_number_of_days,
				      mcm_number_in_bin_2 int default 0 not null constraint mcm_number_in_bin_2_not_null,
				      mcm_paper_in_bin_2_longest varchar(50),
				      mcm_longest_bin2_number_of_days int default 0 not null constraint mcm_longest_bin2_number_of_days,
				      mcm_number_in_bin_3 int default 0 not null constraint mcm_number_in_bin_3_not_null,
				      mcm_paper_in_bin_3_longest varchar(50),
				      mcm_longest_bin3_number_of_days int default 0 not null constraint mcm_longest_bin3_number_of_days,
				      mcm_number_in_phenotype_bin int default 0 not null constraint mcm_number_in_phenotype_bin_not_null,
				      mcm_paper_in_phenotype_bin_longest varchar(50),
				      mcm_longest_phenotype_number_of_days int default 0 not null constraint mcm_longest_phenotype_number_of_days,
				      mcm_number_in_expression_bin int default 0 not null constraint mcm_number_in_expression_bin_not_null,
				      mcm_paper_in_expression_bin_longest varchar(50),
				      mcm_longest_expression_number_of_days int default 0 not null constraint mcm_longest_expression_number_of_days,
				      mcm_number_in_ortho_bin int default 0 not null constraint mcm_number_in_expression_bin_not_null,
				      mcm_paper_in_ortho_bin_longest varchar(50),
				      mcm_longest_ortho_number_of_days int default 0 not null constraint mcm_longest_ortho_number_of_days,
				      mcm_number_archived int default 0 not null constraint mcm_number_archived_not_null,
				      mcm_number_closed_unread int default 0 not null constraint mcm_number_closed_unread_not_null,
				      mcm_average_time_in_bins int default 0 not null constraint mcm_average_time_in_bins_not_null,
				      mcm_average_time_in_curating_status int default 0 not null constraint mcm_average_time_in_curating_status_not_null,
				      mcm_average_time_waiting_for_nomenclature int default 0 not null constraint mcm_average_time_waiting_for_nomenclature_not_null,
				      mcm_average_time_waiting_for_software_fix int default 0 not null constraint mcm_average_time_waiting_for_software_fix_not_null,
				      mcm_average_time_waiting_for_author int default 0 not null constraint mcm_average_time_waiting_for_author_not_null,
				      mcm_date_captured datetime year to second default current year to second not null constraint mcm_date_captured_not_null)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create unique index monthly_curated_metric_pk_index
  on monthly_curated_metric (mcm_pk_id)
 using btree in idxdbs1;

alter table monthly_curated_metric
 add constraint primary key (mcm_pk_id)
 constraint monthly_curated_metric_primary_key;


