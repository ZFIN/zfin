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

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ('f',current year to second)
 where zflag_name = "regen_constructmart";