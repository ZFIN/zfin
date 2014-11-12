

insert into xpat_exp_details_generated_temp (xedg_zdb_id, 
       	    			   xedg_fig_zdb_id, xedg_assay_name,
 	xedg_probe_feature_zdb_id, xedg_gene_zdb_id,
	xedg_direct_submission_date, xedg_dblink_zdb_id,
	xedg_genox_zdb_id, xedg_atb_zdb_id)
 select distinct xpatex_zdb_id, 
 	xpatfig_fig_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id
   from expression_experiment, expression_result, expression_pattern_figure
   where xpatex_zdb_id = xpatres_xpatex_zdb_id
   and xpatres_zdb_id = xpatfig_xpatres_Zdb_id;


insert into xpat_results_generated_temp (xrg_zdb_id,xrg_xedg_id, xrg_xedg_zdb_id, xrg_start_stg_zdb_id,
       	    		       	xrg_end_stg_zdb_id, xrg_expression_found,
				xrg_comments, xrg_suggested_anatomy_item,
				xrg_superterm_zdb_id, xrg_subterm_zdb_id)
select xpatres_zdb_id,xedg_pk_id, xpatres_xpatex_zdb_id, xpatres_start_stg_zdb_id,
       	    		       	xpatres_end_stg_zdb_id, xpatres_expression_found,
				xpatres_comments, xpatres_suggested_anatomy_item,
				xpatres_superterm_zdb_id, xpatres_subterm_zdb_id
  from expression_result,expression_pattern_figure,xpat_exp_Details_generated_temp
  where xpatres_zdb_id = xpatfig_xpatres_zdb_id
  and xpatres_xpatex_zdb_id = xedg_zdb_id
  and xpatfig_fig_zdb_id = xedg_fig_zdb_id;

