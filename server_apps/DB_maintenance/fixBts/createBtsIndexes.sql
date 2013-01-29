begin work;

create index fish_annotation_search_fas_all_bts_index
  on fish_annotation_search (fas_pheno_term_group bts_lvarchar_ops,
			     fas_all bts_lvarchar_ops,
			     fas_affector_type_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="50000") IN smartbs1;


create index figure_term_fish_search_term_group_bts_index
  on figure_term_fish_search (ftfs_term_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs1;

create index construct_search_all_names_bts_index
  on construct_search (cons_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;


create index construct_component_search_all_promoter_names_bts_index
  on construct_component_search (ccs_promoter_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

create index construct_component_search_all_coding_names_bts_index
  on construct_component_search (ccs_coding_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

create index construct_component_search_all_engineered_names_bts_index
  on construct_component_search (ccs_engineered_region_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

create index construct_gene_feature_result_view_all_allele_gene_names_bts_index
  on construct_gene_feature_result_view (cgfrv_allele_gene_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

create index figure_term_construct_search_bts_index
  on figure_term_construct_search (ftcs_term_group bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;

commit work;