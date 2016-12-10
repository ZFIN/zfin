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
       	     			      mcm_pub_arrival_date_month varchar(10) not null constraint mcm_pub_arrival_date_month_not_null,
				      mcm_pub_arrival_date_year varchar(10) not null constraint mcm_pub_arrival_date_year_not_null,
       	     			      mcm_number_in_bin_1 int default 0 not null constraint mcm_number_in_bin_1_not_null,
				      mcm_number_in_bin_2 int default 0 not null constraint mcm_number_in_bin_2_not_null,				
				      mcm_number_in_bin_3 int default 0 not null constraint mcm_number_in_bin_3_not_null,
				      mcm_number_in_phenotype_bin int default 0 not null constraint mcm_number_in_phenotype_bin_not_null,
				      mcm_number_in_expression_bin int default 0 not null constraint mcm_number_in_expression_bin_not_null,
				      mcm_number_in_ortho_bin int default 0 not null constraint mcm_number_in_ortho_bin_not_null,
				      mcm_number_archived_this_month int default 0 not null constraint mcm_number_archived_not_null,
				      mcm_number_closed_unread_this_month int default 0 not null constraint mcm_number_closed_unread_not_null,
				      mcm_number_closed_curated_this_month int default 0 not null constraint mcm_number_closed_curated_not_null,
				      mcm_date_captured datetime year to second default current year to second not null constraint mcm_date_captured_not_null 
				      )
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;


create unique index monthly_curated_metric_pk_index
  on monthly_curated_metric (mcm_pk_id)
 using btree in idxdbs1;

alter table monthly_curated_metric
 add constraint primary key (mcm_pk_id)
 constraint monthly_curated_metric_primary_key;

create table longest_bin_resident_metric (lbrm_pk_id serial8 not null constraint lbrm_pk_id_not_null,
       	     			      lbrm_date_Captured datetime year to second default current year to second not null constraint lbrm_date_captured_not_null,
				      lbrm_pub_zdb_id varchar(50) not null constraint lbrm_pub_Zdb_id_not_null,
				      lbrm_status_counted varchar(20) not null constraint lbrm_status_counted_not_null,
				      lbrm_days_in_status_on_this_date int not null constraint lbrm_days_in_this_status_on_this_date_not_null)
fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create table monthly_average_curated_metric (macm_pk_id serial8 not null constraint macm_pk_id_not_null,
       	     			      macm_date_captured datetime year to second default current year to second not null constraint macm_date_captured_not_null,
       	     			      macm_average_stay_in_bin_1 int  default 0  not null constraint macm_average_stay_in_bin_1_not_null,
				      macm_average_stay_in_bin_2 int  default 0  not null constraint macm_average_Stay_in_bin_2_not_null,
				      macm_average_stay_in_bin_3 int  default 0  not null constraint macm_average_Stay_in_bin_3_not_null,
				      macm_average_stay_in_pheno_bin int  default 0 not null constraint macm_average_Stay_in_pheno_not_null,
				      macm_average_stay_in_xpat_bin int  default 0 not null constraint macm_average_Stay_in_xpat_not_null,
				      macm_average_stay_in_ortho_bin int  default 0 not null constraint macm_average_Stay_in_ortho_not_null,
				      macm_paper_in_bin_1_longest varchar(50),
				      macm_longest_bin1_number_of_days int default 0 not null constraint macm_longest_bin1_number_of_days, 
				      macm_paper_in_bin_2_longest varchar(50),
				      macm_longest_bin2_number_of_days int default 0 not null constraint macm_longest_bin2_number_of_days,
  			    	      macm_paper_in_bin_3_longest varchar(50),
				      macm_longest_bin3_number_of_days int default 0 not null constraint macm_longest_bin3_number_of_days,
				      macm_paper_in_phenotype_bin_longest varchar(50),
				      macm_longest_phenotype_number_of_days int default 0 not null constraint macm_longest_phenotype_number_of_days,
				      macm_paper_in_expression_bin_longest varchar(50),
				      macm_longest_expression_number_of_days int default 0 not null constraint macm_longest_expression_number_of_days,
 				      macm_paper_in_ortho_bin_longest varchar(50),
				      macm_longest_ortho_number_of_days int default 0 not null constraint macm_longest_ortho_number_of_days)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create table average_curation_time_metric (actm_pk_id serial8 not null constraint actm_pk_id_not_null,
       	     				   actm_average_time_in_curating_status int default 0 not null constraint actm_average_time_in_curating_status_not_null,
					 
					   actm_stddev_time_in_curating_status int default 0 not null constraint actm_stddev_time_in_curating_status_not_null,
					   actm_average_time_in_bin1 int default 0 not null constraint actm_average_time_in_bin1_status_not_null,				
					   
					   actm_stddev_in_bin1 int default 0 not null constraint actm_stddev_time_in_bin1_status_not_null,
					   actm_average_time_in_bin2 int default 0 not null constraint actm_average_time_in_bin2_status_not_null,				
					   
					   actm_stddev_in_bin2 int default 0 not null constraint actm_stddev_time_in_bin2_status_not_null,
					   actm_average_time_in_bin3 int default 0 not null constraint actm_average_time_in_bin3_status_not_null,				
					   
					   actm_stddev_in_bin3 int default 0 not null constraint actm_stddev_time_in_bin3_status_not_null,
					   actm_average_time_in_xpat int default 0 not null constraint actm_average_time_in_xpat_status_not_null,				
					   
					   actm_stddev_in_xpat int default 0 not null constraint actm_stddev_time_in_xpat_status_not_null,
					   actm_average_time_in_pheno int default 0 not null constraint actm_average_time_in_pheno_status_not_null,				
					  
					   actm_stddev_in_pheno int default 0 not null constraint actm_stddev_time_in_pheno_status_not_null,
					   actm_average_time_in_ortho int default 0 not null constraint actm_average_time_in_ortho_status_not_null,				
					   
					   actm_stddev_in_ortho int default 0 not null constraint actm_stddev_time_in_ortho_status_not_null,
				      	   actm_average_time_waiting_for_nomenclature int default 0 not null constraint actm_average_time_waiting_for_nomenclature_not_null,
					   actm_stdev_time_waiting_for_nomenclature int default 0 not null constraint actm_stdev_time_waiting_for_nomenclature_not_null,
				      	   actm_average_time_waiting_for_software_fix int default 0 not null constraint actm_average_time_waiting_for_software_fix_not_null,
					   actm_stdev_time_waiting_for_software_fix int default 0 not null constraint actm_sdtev_time_waiting_for_software_fix_not_null,
				     	   actm_average_time_waiting_for_author int default 0 not null constraint actm_average_time_waiting_for_author_not_null,
				     	   actm_stdev_time_waiting_for_author int default 0 not null constraint actm_stdev_time_waiting_for_author_not_null,
				      	   actm_average_time_waiting_for_ontology int default 0 not null constraint actm_average_time_waiting_for_ontology_not_null,
					   actm_stdev_time_waiting_for_ontology int default 0 not null constraint actm_stdev_time_waiting_for_ontology_not_null,
				      	   actm_average_time_waiting_for_curator_review int default 0 not null constraint actm_average_time_waiting_for_curator_review_not_null,
					   actm_stdev_time_waiting_for_curator_review int default 0 not null constraint actm_stdev_time_waiting_for_curator_review_not_null,
				      	   actm_date_captured datetime year to second default current year to second not null constraint actm_date_captured_not_null)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;

create table average_time_per_person (actpp_pk_id serial8 not null constraint actpp_pk_id_not_null,
       	     				       actpp_date_captured datetime year to second default current year to second  not null constraint actpp_date_captured_not_null,
					       actpp_person_zdb_id varchar(50) not null constraint actpp_person_zdb_id_not_null,
					       actpp_curation_type varchar(20) not null constraint actpp_curation_type_not_null,
					       actpp_average_time_spent_days int default 0 not null constraint actpp_average_time_spent_days_not_null)
 fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 2048 next size 2048;
					       
