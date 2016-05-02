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

--drop index expression_experiment_marker_foreign_key_index ;
--drop index expression_experiment_primary_key_index ;
--drop index expression_experiment_source_foreign_key_index ;
--drop index xpatex_atb_zdb_id_index;
--drop index xpatex_featexp_zdb_id_foreign_key_index;
--drop index xpatex_gene_zdb_id_foreign_key_index ;
--drop index expression_result_end_stg_foreign_key_index; 
--drop index expression_result_primary_key_index ;
--drop index expression_result_start_stg_foreign_key_index ;
--drop index expression_result_subterm_foreign_key_index ;
--drop index expression_result_superterm_foreign_key_index; 
--drop index expression_result_xpatex_foreign_key_index;
--drop index expression_pattern_figure_fig_foreign_key ;
--drop index expression_pattern_figure_xpatres_foreign_key_index ;

!time;


update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_expressionmart" ;

delete from expression_experiment;
delete from expression_result;
delete from expression_pattern_figure;


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

insert into expression_result (xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id,xpatres_fig_Zdb_id)
select xpatres_zdb_id,
       xpatres_xpatex_zdb_id,
       xpatres_start_stg_zdb_id,
       xpatres_end_stg_zdb_id,
       xpatres_expression_found,
       xpatres_superterm_Zdb_id,
       xpatres_subterm_Zdb_id, xpatres_Fig_zdb_id  from expression_result_temp;

insert into expression_pattern_figure (xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id)
 select xpatfig_xpatres_zdb_id, xpatfig_fig_zdb_id from expression_pattern_figure_temp;


update statistics high for table expression_experiment;
update statistics high for table expression_result;
update statistics high for table expression_pattern_Figure;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_expressionmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "expression mart";
