SELECT xpatfig_fig_zdb_id,
       fig_source_zdb_id,
       xpatfig_xpatres_zdb_id,
       xpatex_source_zdb_id
FROM   figure,
       expression_pattern_figure,
       expression_result,
       expression_experiment
WHERE  xpatfig_fig_zdb_id = fig_zdb_id
       AND xpatfig_xpatres_zdb_id = xpatres_zdb_id
       AND xpatres_xpatex_zdb_id = xpatex_zdb_id
       AND xpatex_source_zdb_id != fig_source_zdb_id ;