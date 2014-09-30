SELECT DISTINCT xpatfig_fig_zdb_id, 
                xpatex_source_zdb_id, 
                xpatex_zdb_id, 
                xpatex_probe_feature_zdb_id, 
                xpatex_gene_zdb_id 
FROM   expression_experiment, 
       expression_result, 
       expression_pattern_figure 
WHERE  xpatex_probe_feature_zdb_id IS NOT NULL 
       AND NOT EXISTS (SELECT * 
                       FROM   marker_relationship 
                       WHERE  xpatex_probe_feature_zdb_id = mrel_mrkr_2_zdb_id 
                              AND xpatex_gene_zdb_id = mrel_mrkr_1_zdb_id) 
       AND xpatfig_xpatres_zdb_id = xpatres_zdb_id 
       AND xpatres_xpatex_zdb_id = xpatex_zdb_id; 