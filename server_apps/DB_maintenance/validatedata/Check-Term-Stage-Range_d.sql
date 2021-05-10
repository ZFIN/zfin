SELECT fig_source_zdb_id,
   fig_zdb_id,
   s1.stg_name_long,
   s2.stg_name_long,
   xpatres_superterm_zdb_id,
   super.term_ont_id,
   super.term_name,
   xpatres_subterm_zdb_id,
   sub.term_ont_id,
   sub.term_name,
   s3.stg_name_long,
   s4.stg_name_long,
   xpatres_pk_id
FROM   expression_result2
   LEFT OUTER JOIN term sub
      ON xpatres_subterm_zdb_id = sub.term_zdb_id
   JOIN expression_figure_stage
      ON xpatres_efs_id = efs_pk_id
   JOIN figure
      ON efs_fig_zdb_id = fig_zdb_id
   JOIN term super
      ON xpatres_superterm_zdb_id = super.term_zdb_id
   JOIN stage s1
      ON efs_start_stg_zdb_id = s1.stg_zdb_id
   JOIN stage s2
      ON efs_end_stg_zdb_id = s2.stg_zdb_id
   JOIN term_stage
      ON super.term_zdb_id = ts_term_zdb_id
   JOIN stage s3
      ON ts_start_stg_zdb_id = s3.stg_zdb_id
   JOIN stage s4
      ON ts_end_stg_zdb_id = s4.stg_zdb_id
WHERE  Aoterm_overlaps_stg_window(xpatres_superterm_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id) = 'f'  
