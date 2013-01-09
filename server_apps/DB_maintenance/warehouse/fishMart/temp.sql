begin work ;

drop index fish_annotation_search_fas_all_bts_index;
drop index figure_term_fish_search_term_group_bts_index;


update fish_annotation_search
 set fas_genotype_group = fas_line_handle
where fas_genotype_group is null;

create index fish_annotation_search_fas_all_bts_index
  on fish_annotation_search (fas_pheno_term_group bts_lvarchar_ops,
			     fas_all bts_lvarchar_ops,
			     fas_affector_type_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="50000") IN smartbs_bts;


create index figure_term_fish_search_term_group_bts_index
  on figure_term_fish_search (ftfs_term_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;



commit work ;