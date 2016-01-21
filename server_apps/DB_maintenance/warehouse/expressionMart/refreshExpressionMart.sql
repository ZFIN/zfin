--drop FKs.


delete from expression_experiment_bkup;
delete from expression_result_bkup;
delete from expression_pattern_figure_bkup;

insert into expression_experiment_bkup (xpatex_zdb_id, 
       	    xpatex_source_zdb_id,
	    xpatex_assay_name,
	    xpatex_probe_feature_zdb_id,
	    xpatex_gene_zdb_id,
	    xpatex_direct_submission_date,
	    xpatex_dblink_zdb_id,
	    xpatex_genox_zdb_id,
	    xpatex_atb_Zdb_id)
 select xpatex_zdb_id, 
       	    xpatex_source_zdb_id,
	    xpatex_assay_name,
	    xpatex_probe_feature_zdb_id,
	    xpatex_gene_zdb_id,
	    xpatex_direct_submission_date,
	    xpatex_dblink_zdb_id,
	    xpatex_genox_zdb_id,
	    xpatex_atb_Zdb_id from expression_experiment;

insert into expression_result_bkup (xpatres_pk_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stage_zdb_id,
       xpatres_end_stage_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id)
select xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id  from expression_result;

insert into expression_pattern_figure_bkup (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
 select xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id from expression_pattern_figure;



!time;

delete from expression_experiment;
insert into expression_experiment (xpatex_zdb_id, 
       	    xpatex_source_zdb_id,
	    xpatex_assay_name,
	    xpatex_probe_feature_zdb_id,
	    xpatex_gene_zdb_id,
	    xpatex_direct_submission_date,
	    xpatex_dblink_zdb_id,
	    xpatex_genox_zdb_id,
	    xpatex_atb_Zdb_id)
 select xpatex_zdb_id, 
       	    xpatex_source_zdb_id,
	    xpatex_assay_name,
	    xpatex_probe_feature_zdb_id,
	    xpatex_gene_zdb_id,
	    xpatex_direct_submission_date,
	    xpatex_dblink_zdb_id,
	    xpatex_genox_zdb_id,
	    xpatex_atb_Zdb_id from expression_experiment_temp;

delete from expression_result;
insert into expression_result (xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id)
 select xpatres_pk_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stage_zdb_id,
       xpatres_end_stage_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id  from expression_result_temp;

delete from expression_pattern_figure;
 insert into expression_pattern_figure (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
 select xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id from expression_pattern_figure_temp;

