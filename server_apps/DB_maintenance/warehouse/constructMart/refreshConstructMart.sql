--drop FKs.

delete from construct_Search_backup;
delete from construct_component_search_backup;
delete from construct_gene_feature_result_view_backup;
delete from figure_term_construct_search_backup;

drop index construct_search_all_names_bts_index;

drop index construct_component_search_all_promoter_names_bts_index;

drop index construct_component_search_all_coding_names_bts_index;

drop index construct_component_search_all_engineered_names_bts_index;

drop index construct_gene_feature_result_view_all_allele_gene_names_bts_index;

drop index figure_term_construct_search_bts_index;

insert into construct_search_backup
select * from construct_search;

insert into construct_component_search_backup
select * from construct_component_search;

insert into construct_gene_feature_result_view_backup
select * from construct_gene_feature_Result_View;

insert into figure_term_construct_Search_backup
select * from figure_term_construct_Search;

delete from construct_search;
insert into construct_search
 select * from construct_search_temp;

delete from construct_gene_feature_result_view;
insert into construct_gene_feature_result_view
  select * from construct_gene_feature_result_view_temp;

delete from figure_term_construct_search;
insert into figure_term_construct_search
  select * from figure_term_construct_search_temp; 

delete from construct_component_search;
insert into construct_component_search
  select * from construct_component_search_temp;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_constructmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "construct mart";