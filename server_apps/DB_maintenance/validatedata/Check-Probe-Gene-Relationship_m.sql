SELECT DISTINCT efs_fig_zdb_id,
                xpatex_source_zdb_id, 
                xpatex_zdb_id, 
                xpatex_probe_feature_zdb_id,
                probe.mrkr_abbrev,
                xpatex_gene_zdb_id,
                gene.mrkr_abbrev
FROM   expression_experiment2,
       expression_result2,
       expression_figure_stage,
       marker as probe,
       marker as gene
WHERE  xpatex_probe_feature_zdb_id IS NOT NULL
       AND NOT EXISTS (SELECT * 
                       FROM   marker_relationship 
                       WHERE  xpatex_probe_feature_zdb_id = mrel_mrkr_2_zdb_id 
                       AND    xpatex_gene_zdb_id = mrel_mrkr_1_zdb_id)
       AND xpatres_efs_id = efs_pk_id
       AND efs_xpatex_zdb_id = xpatex_zdb_id
       AND gene.mrkr_zdb_id = xpatex_gene_zdb_id
       AND probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
       AND gene.mrkr_abbrev not like '%WITHDRAWN%';