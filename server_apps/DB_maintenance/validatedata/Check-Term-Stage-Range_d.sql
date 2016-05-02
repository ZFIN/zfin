select
   fig_source_zdb_id,
   fig_zdb_id,
   s1.stg_name_long,
   s2.stg_name_long,
   xpatres_superterm_zdb_id,
   term_ont_id,
   term_name,
   s3.stg_name_long,
   s4.stg_name_long,
   xpatres_pk_id
from
   expression_result2,
   term_stage,
   stage s1,
   stage s2,
   stage s3,
   stage s4,
   term,
   figure,
   expression_figure_stage
where
   aoterm_overlaps_stg_window(xpatres_superterm_zdb_id, efs_start_stg_zdb_id, efs_end_stg_zdb_id) = "f"
   and ts_start_stg_zdb_id = s3.stg_zdb_id
   and ts_end_stg_zdb_id = s4.stg_zdb_id
   and term_zdb_id = ts_term_zdb_id
   and xpatres_superterm_zdb_id = term_zdb_id
   and efs_start_stg_zdb_id = s1.stg_zdb_id
   and efs_end_stg_zdb_id = s2.stg_zdb_id
   and efs_fig_zdb_id = fig_zdb_id
   and xpatres_efs_id = efs_pk_id