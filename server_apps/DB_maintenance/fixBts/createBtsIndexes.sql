begin work;


create index "informix".construct_search_all_names_bts_index 
    on "informix".construct_search (cons_all_names bts_lvarchar_ops) 
    using bts (query_default_field='*', analyzer='whitespace', 
    max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_all_coding_names_bts_index 
    on "informix".construct_component_search (ccs_coding_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_all_engineered_names_bts_index 
    on "informix".construct_component_search (ccs_engineered_region_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_all_promoter_names_bts_index 
    on "informix".construct_component_search (ccs_promoter_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".construct_gene_feature_result_view_all_allele_gene_names_bts_index 
    on "informix".construct_gene_feature_result_view (cgfrv_allele_gene_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".figure_term_construct_search_bts_index 
    on "informix".figure_term_construct_search (ftcs_term_group 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;


create index "informix".construct_search_temp_all_names_bts_index 
    on "informix".construct_search_temp (cons_all_names bts_lvarchar_ops) 
    using bts (query_default_field='*', analyzer='whitespace', 
    max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_temp_all_coding_names_bts_index 
    on "informix".construct_component_search_temp (ccs_coding_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_temp_all_engineered_names_bts_index 
    on "informix".construct_component_search_temp (ccs_engineered_region_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".construct_component_search_temp_all_promoter_names_bts_index 
    on "informix".construct_component_search_temp (ccs_promoter_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;


create index "informix".construct_gene_feature_result_view_temp_all_allele_gene_names_bts_index 
    on "informix".construct_gene_feature_result_view_temp (cgfrv_allele_gene_all_names 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;

create index "informix".figure_term_construct_search_temp_bts_index 
    on "informix".figure_term_construct_search_temp (ftcs_term_group 
    bts_lvarchar_ops) using bts (query_default_field='*', analyzer=
    'whitespace', max_clause_count='10000') in smartbs_bts;


commit work;