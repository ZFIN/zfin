--drop FKs.

alter table gene_feature_result_view
  drop constraint gfrv_fas_foreign_key;

alter table figure_term_fish_search
  drop constraint ftfs_fas_foreign_key;

delete from fish_annotation_Search_backup;
delete from gene_feature_result_view_backup;
delete from figure_term_fish_search_backup;

insert into fish_annotation_search_backup
select * from fish_annotation_Search;

insert into gene_feature_result_view_backup
select * from gene_feature_Result_View;

insert into figure_term_Fish_Search_backup
select * from figure_term_Fish_Search;

delete from fish_annotation_search;
insert into fish_annotation_search
 select * from fish_annotation_search_temp;

delete from gene_feature_result_view;
insert into gene_feature_result_view
  select * from gene_feature_result_view_temp;

delete from figure_term_fish_search;
insert into figure_term_fish_search
  select * from figure_term_fish_search_temp; 

alter table gene_feature_Result_view
  add constraint (Foreign key (gfrv_fas_id)
  references fish_annotation_search
  on delete cascade constraint gfrv_fas_foreign_key);

alter table figure_term_Fish_search
  add constraint (Foreign key (ftfs_fas_id)
  references fish_annotation_search on delete cascade
   constraint ftfs_Fas_foreign_key);


update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_fishmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "fish mart";