begin work ;

create temp table tmp_update_fig (fig_id varchar(50))
with no log ;

load from updateList.unl
  insert into tmp_update_fig ;

create unique index fig_id_index
  on tmp_update_fig (fig_id)
  using btree in idxdbs3 ;

update statistics high for table tmp_update_fig ;

update figure
  set fig_comments = 'PNAS Report Generated'||" "||CURRENT
  where fig_Zdb_id in (select fig_id
			from tmp_update_fig) ;

commit work ;
