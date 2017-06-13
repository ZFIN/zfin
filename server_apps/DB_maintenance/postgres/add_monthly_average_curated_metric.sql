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
