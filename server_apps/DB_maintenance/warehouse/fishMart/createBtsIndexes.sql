update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_fishmart_bts_indexes";

create index fish_annotation_search_fas_all_bts_index
  on fish_annotation_search (fas_pheno_term_group bts_lvarchar_ops,
			     fas_all bts_lvarchar_ops,
			     fas_affector_type_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="50000") IN smartbs_bts;


create index figure_term_fish_search_term_group_bts_index
  on figure_term_fish_search (ftfs_term_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_fishmart_bts_indexes";