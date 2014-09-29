SELECT xpatex_zdb_id,
       xpatex_probe_feature_zdb_id,
       xpatex_gene_zdb_id
FROM   expression_experiment
WHERE  xpatex_probe_feature_zdb_id IS NOT NULL
       AND NOT EXISTS (SELECT *
                       FROM   marker_relationship
                       WHERE  xpatex_probe_feature_zdb_id = mrel_mrkr_2_zdb_id
                              AND xpatex_gene_zdb_id = mrel_mrkr_1_zdb_id);