


insert into expression_experiment_temp (xpatex_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id, xpatex_source_zdb_id)
 select distinct xpatex_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id, xpatex_source_zdb_id
   from expression_Experiment2;

insert into expression_Result_temp (
    xpatres_xpatex_zdb_id,
    xpatres_start_stg_zdb_id,
    xpatres_end_stg_zdb_id,
    xpatres_expression_found,
    xpatres_superterm_zdb_id,
    xpatres_subterm_zdb_id,
    xpatres_fig_zdb_id)
 select distinct
 		 efs_xpatex_zdb_id, 
		 efs_start_stg_zdb_id, 
		 efs_end_stg_zdb_id, 
		 xpatres_expression_found, 
    		 xpatres_superterm_zdb_id,
    		 xpatres_subterm_zdb_id,
		 efs_fig_zdb_id
  from expression_result2, expression_figure_stage
 where xpatres_efs_id = efs_pk_id;


insert into expression_pattern_figure_temp (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
 select distinct xpatres_zdb_id,xpatres_fig_zdb_id
   from expression_result_temp, expression_figure_stage
 where xpatres_xpatex_zdb_id = efs_xpatex_zdb_id
 and xpatres_start_stg_zdb_id = efs_start_stg_zdb_id
 and xpatres_end_stg_zdb_id = efs_end_stg_zdb_id ;

update statistics high for table expression_experiment_temp;
update statistics high for table expression_result_temp;
update statistics high for table expression_pattern_figure_temp;