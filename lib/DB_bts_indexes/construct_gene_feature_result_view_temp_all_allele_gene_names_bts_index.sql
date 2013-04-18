create index construct_gene_feature_result_view_temp_all_allele_gene_names_bts_index

  on construct_gene_feature_result_view_temp (cgfrv_allele_gene_all_names bts_lvarchar_ops) USING BTS(query_default_field="*", analyzer="whitespace",max_clause_count="10000") IN smartbs_bts;
