SELECT efs_fig_zdb_id,
       fig_source_zdb_id,
       xpatres_pk_id,
       xpatex_source_zdb_id
FROM   figure,
       expression_figure_stage,
       expression_result2,
       expression_experiment2
WHERE  efs_fig_zdb_id = fig_zdb_id
       AND xpatres_efs_id = efs_pk_id
       AND efs_xpatex_zdb_id = xpatex_zdb_id
       AND xpatex_source_zdb_id != fig_source_zdb_id ;