unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
	select fig_source_zdb_id,fig_zdb_id,s1.stg_zdb_id,
                   s1.stg_name_long, s2.stg_zdb_id,s2.stg_name_long,
                   xpatres_superterm_zdb_id, term_ont_id, term_name,
                   s3.stg_name_long, s4.stg_name_long, xpatres_zdb_id
	      from expression_result, term_stage, stage s1, stage s2,stage s3, stage s4, term, figure, expression_pattern_figure
             where aoterm_overlaps_stg_window(
                                     xpatres_superterm_zdb_id,
                                     xpatres_start_stg_zdb_id,
                                     xpatres_end_stg_zdb_id
                                     ) = "f"
                        and ts_start_stg_zdb_id = s3.stg_zdb_id
                        and ts_end_stg_zdb_id = s4.stg_zdb_id
                        and term_zdb_id = ts_term_zdb_id
                        and xpatres_superterm_zdb_id = term_zdb_id
                        and xpatres_start_stg_zdb_id = s1.stg_zdb_id
                        and xpatres_end_stg_zdb_id = s2.stg_zdb_id
			and xpatfig_fig_zdb_id = fig_zdb_id
			and xpatfig_xpatres_zdb_id = xpatres_zdb_id

