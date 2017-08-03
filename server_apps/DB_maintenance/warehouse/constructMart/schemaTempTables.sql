begin work ;

create table construct_search_backup (cons_pk_id serial8 not null constraint consp_pk_id_not_null,
                              cons_construct_zdb_id varchar(50) not null constraint consp_construct_zdb_id_not_null,
                              cons_type varchar(50),
                              cons_all_names lvarchar(16000),
                              cons_all_with_spaces lvarchar(10000),
                              cons_name varchar(255),
                              cons_abbrev varchar(255),
                              cons_abbrev_order varchar(255)
                              )
 fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 16384 next size 16384 lock mode row;

--create unique index consp_pk_id_primary_key_index
--  on construct_search_temp (cons_pk_id)
--  using btree in idxdbs1;

--create unique index consp_alternate_key_index
--  on construct_search_temp (cons_construct_zdb_id)
--  using btree in idxdbs1;

create table construct_search_temp (cons_pk_id serial8 not null constraint const_pk_id_not_null,
       	     		      cons_construct_zdb_id varchar(50) not null constraint const_construct_zdb_id_not_null,
			      cons_type varchar(50),
			      cons_all_names lvarchar(16000),
			      cons_all_with_spaces lvarchar(10000),
			      cons_name varchar(255),
			      cons_abbrev varchar(255),
			      cons_abbrev_order varchar(255)
			      )
 fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;

--create unique index const_pk_id_primary_key_index
--  on construct_search_temp (cons_pk_id)
--  using btree in idxdbs1;

--create unique index const_alternate_key_index
--  on construct_search_temp (cons_construct_zdb_id)
--  using btree in idxdbs1;

create table construct_component_search_backup (ccs_pk_id serial8
                                                        not null constraint ccsb_pk_id_not_null,
                                         ccs_cons_id int8 not null constraint ccsb_cons_pk_id_not_null,
                                         ccs_gene_zdb_id varchar(50),
                                         ccs_gene_abbrev varchar(255),
                                         ccs_gene_abbrev_order varchar(255),
                                         ccs_promoter_all_names lvarchar(15000),
                                         ccs_coding_all_names lvarchar(15000),
                                         ccs_engineered_region_all_names lvarchar,
                                         ccs_relationship_type varchar(40))
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
 extent size 16384 next size 16384 lock mode row;


--create unique index ccsb_primary_key_index
--  on construct_component_search_backup (ccs_pk_id)
--  using btree in idxdbs3;

--create index ccsb_cons_id_foreign_key_index
--  on construct_component_search_backup (ccs_cons_id)
--  using btree in idxdbs2;

--create index ccsb_gene_zdb_id_index
--  on construct_component_search_backup (ccs_gene_zdb_id)
--using btree in idxdbs1;


create table construct_component_search_temp (ccs_pk_id serial8
							not null constraint ccst_pk_id_not_null,
					 ccs_cons_id int8 not null constraint ccst_cons_pk_id_not_null,
					 ccs_gene_zdb_id varchar(50),
					 ccs_gene_abbrev varchar(255),
					 ccs_gene_abbrev_order varchar(255),
					 ccs_promoter_all_names lvarchar(15000),
					 ccs_coding_all_names lvarchar(15000),
					 ccs_engineered_region_all_names lvarchar,
					 ccs_relationship_type varchar(40))
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
 extent size 16384 next size 16384 lock mode row;


--create unique index ccst_primary_key_index
--  on construct_component_search_temp (ccs_pk_id)
--  using btree in idxdbs3;

--create index ccst_cons_id_foreign_key_index
--  on construct_component_search_Temp (ccs_cons_id)
--  using btree in idxdbs2;

--create index ccst_gene_zdb_id_index
--  on construct_component_search_temp (ccs_gene_zdb_id)
--using btree in idxdbs1;					 


create table construct_gene_feature_result_view_backup (cgfrv_pk_id serial8 not null constraint cgfrvb_pk_id_not_null,
                                cgfrv_cs_id int8 not null constraint cgfrvb_cs_id_not_null,
                                cgfrv_allele_gene_zdb_id varchar(50),
                                cgfrv_allele_gene_abbrev varchar(255),
                                cgfrv_allele_gene_all_names lvarchar(15000),
                                cgfrv_allele_gene_order varchar(255),
                                cgfrv_feature_zdb_id varchar(50),
                                cgfrv_feature_abbrev varchar(50),
                                cgfrv_feature_name varchar(255),
                                cgfrv_feature_order varchar(255),
                                cgfrv_lab_of_origin varchar(50),
                                cgfrv_lab_name varchar(255),
                                cgfrv_available varchar(50))
 fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 16384 next size 16384 lock mode row;

create unique index cgfrvb_primary_key_index
  on construct_gene_feature_result_view_backup (cgfrv_pk_id)
  using btree in idxdbs3;

create index cgfrvb_cs_id_foreign_key_index
  on construct_gene_feature_result_view_backup (cgfrv_cs_id)
  using btree in idxdbs2;


create index cgfrvb_allele_gene_zdb_id_index
 on construct_gene_feature_result_view_backup (cgfrv_allele_gene_zdb_id)
using btree in idxdbs1;


create table construct_gene_feature_result_view_temp (cgfrv_pk_id serial8 not null constraint cgfrvt_pk_id_not_null,
       	     			cgfrv_cs_id int8 not null constraint cgfrvt_cs_id_not_null,
				cgfrv_allele_gene_zdb_id varchar(50),
				cgfrv_allele_gene_abbrev varchar(255),
				cgfrv_allele_gene_all_names lvarchar(15000),
				cgfrv_allele_gene_order varchar(255),
				cgfrv_feature_zdb_id varchar(50),
				cgfrv_feature_abbrev varchar(50),
				cgfrv_feature_name varchar(255),
				cgfrv_feature_order varchar(255),
				cgfrv_lab_of_origin varchar(50), 
				cgfrv_lab_name varchar(255),
				cgfrv_available varchar(50))
 fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;

create unique index cgfrvt_primary_key_index
 on construct_gene_feature_result_view_temp (cgfrv_pk_id)
 using btree in idxdbs3;

create index cgfrvt_cs_id_foreign_key_index
  on construct_gene_feature_result_view_Temp (cgfrv_cs_id)
  using btree in idxdbs2;


create index cgfrvt_allele_gene_zdb_id_index
  on construct_gene_feature_result_view_temp (cgfrv_allele_gene_zdb_id)
using btree in idxdbs1;


create table  figure_term_construct_search_backup (ftcs_pk_id serial8 not null constraint ftcsb_pk_id_not_null,
                                            ftcs_cs_id int8 not null constraint ftcsb_cs_id_not_null,
                                            ftcs_fig_Zdb_id varchar(50) not null constraint ftcsb_fig_zdb_id_not_null,
                                            ftcs_term_group lvarchar(3000),
                                            ftcs_genox_zdb_id varchar(50),
                                            ftcs_geno_handle varchar(255),
                                            ftcs_geno_name varchar(255),
                                            ftcs_has_images boolean default 'f' not null constraint ftcsb_has_images_not_null)
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
  extent size 16384 next size 16384 lock mode row;

create unique index figure_term_construct_searchb_pk_index
  on figure_term_construct_search_backup (ftcs_pk_id)
 using btree in idxdbs3;

create index figure_term_construct_searchb_cs_id_fk_index
  on figure_term_Construct_search_backup (ftcs_cs_id)
  using btree in idxdbs2;



create table  figure_term_construct_search_temp (ftcs_pk_id serial8 not null constraint ftcst_pk_id_not_null,
       	      				    ftcs_cs_id int8 not null constraint ftcst_cs_id_not_null,
					    ftcs_fig_Zdb_id varchar(50) not null constraint ftcst_fig_zdb_id_not_null,
					    ftcs_term_group lvarchar(3000),
					    ftcs_genox_zdb_id varchar(50),
					    ftcs_geno_handle varchar(255),
					    ftcs_geno_name varchar(255),
					    ftcs_has_images boolean default 'f' not null constraint ftcst_has_images_not_null)
  fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3 
  extent size 16384 next size 16384 lock mode row;

create unique index figure_term_construct_searcht_pk_index 
  on figure_term_construct_search_temp (ftcs_pk_id)
 using btree in idxdbs3;

create index figure_term_construct_searcht_cs_id_fk_index
  on figure_term_Construct_search_temp (ftcs_cs_id)
  using btree in idxdbs2;

--- int data supplier is available from ZIRC
--- int data source is lab of origin

commit work;

--rollback work;