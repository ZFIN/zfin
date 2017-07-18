create table monthly_average_curated_metric 
  (
    macm_pk_id serial8 not null ,
    macm_date_captured timestamp 
        default now(),
    macm_average_stay_in_bin_1 integer
        default 0 not null ,
    macm_average_stay_in_bin_2 integer 
        default 0 not null,
    macm_average_stay_in_bin_3 integer 
        default 0 not null,
    macm_average_stay_in_pheno_bin integer 
        default 0 not null,
    macm_average_stay_in_xpat_bin integer 
        default 0 not null,
    macm_average_stay_in_ortho_bin integer 
        default 0 not null,
    macm_paper_in_bin_1_longest text,
    macm_longest_bin1_number_of_days integer 
        default 0 not null,
    macm_paper_in_bin_2_longest text,
    macm_longest_bin2_number_of_days integer 
        default 0 not null,
    macm_paper_in_bin_3_longest text,
    macm_longest_bin3_number_of_days integer 
        default 0 not null ,
    macm_paper_in_phenotype_bin_longest text,
    macm_longest_phenotype_number_of_days integer 
        default 0 not null ,
    macm_paper_in_expression_bin_longest text,
    macm_longest_expression_number_of_days integer 
        default 0 not null ,
    macm_paper_in_ortho_bin_longest text,
    macm_longest_ortho_number_of_days integer 
        default 0 not null 
  );

create table average_curation_time_metric (actm_pk_id BIGSERIAL NOT NULL, 
       	     				  actm_average_time_in_curating_status INT DEFAULT 0 NOT NULL,
					  actm_stddev_time_in_curating_status INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_bin1 INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_bin1 INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_bin2 INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_bin2 INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_bin3 INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_bin3 INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_xpat INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_xpat INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_pheno INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_pheno INT DEFAULT 0 NOT NULL, 
					  actm_average_time_in_ortho INT DEFAULT 0 NOT NULL, 
					  actm_stddev_in_ortho INT DEFAULT 0 NOT NULL, 
					  actm_average_time_waiting_for_nomenclature INT DEFAULT 0 NOT NULL, 
					  actm_stdev_time_waiting_for_nomenclature INT DEFAULT 0 NOT NULL, 
					  actm_average_time_waiting_for_software_fix INT DEFAULT 0 NOT NULL, 
					  actm_stdev_time_waiting_for_software_fix INT DEFAULT 0 NOT NULL, 
					  actm_average_time_waiting_for_author INT DEFAULT 0 NOT NULL, 
					  actm_stdev_time_waiting_for_author INT DEFAULT 0 NOT NULL, 
					  actm_average_time_waiting_for_ontology INT DEFAULT 0 NOT NULL, 
					  actm_stdev_time_waiting_for_ontology INT DEFAULT 0 NOT NULL, 
					  actm_average_time_waiting_for_curator_review INT DEFAULT 0 NOT NULL, 
					  actm_stdev_time_waiting_for_curator_review INT DEFAULT 0 NOT NULL, 
					  actm_date_captured TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL);
