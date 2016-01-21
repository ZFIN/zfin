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



create index expression_experiment_marker_foreign_key_index 
    on expression_experiment (xpatex_probe_feature_zdb_id) 
    using btree  in idxdbs2;
create unique index expression_experiment_primary_key_index 
    on expression_experiment (xpatex_zdb_id) using 
    btree  in idxdbs3;
create index expression_experiment_source_foreign_key_index 
    on expression_experiment (xpatex_source_zdb_id) 
    using btree  in idxdbs2;
create index xpatex_atb_zdb_id_index on expression_experiment 
    (xpatex_atb_zdb_id) using btree  in idxdbs2;
create index xpatex_featexp_zdb_id_foreign_key_index 
    on expression_experiment (xpatex_genox_zdb_id) 
    using btree  in idxdbs2;
create index xpatex_gene_zdb_id_foreign_key_index 
    on expression_experiment (xpatex_gene_zdb_id) using 
    btree  in idxdbs2;

create index expression_result_end_stg_foreign_key_index 
    on expression_result (xpatres_end_stg_zdb_id) using 
    btree  in idxdbs2;
create unique index expression_result_primary_key_index 
    on expression_result (xpatres_zdb_id) using btree 
     in idxdbs2;
create index expression_result_start_stg_foreign_key_index 
    on expression_result (xpatres_start_stg_zdb_id) 
    using btree  in idxdbs2;
create index expression_result_subterm_foreign_key_index 
    on expression_result (xpatres_subterm_zdb_id) using 
    btree  in idxdbs2;
create index expression_result_superterm_foreign_key_index 
    on expression_result (xpatres_superterm_zdb_id) 
    using btree  in idxdbs2;
create index expression_result_xpatex_foreign_key_index 
    on expression_result (xpatres_xpatex_zdb_id) using 
    btree  in idxdbs1;

create index expression_pattern_figure_fig_foreign_key 
    on expression_pattern_figure (xpatfig_fig_zdb_id) 
    using btree  in idxdbs1;

create index expression_pattern_figure_xpatres_foreign_key_index 
    on expression_pattern_figure (xpatfig_xpatres_zdb_id) 
    using btree  in idxdbs3;

!time;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("f",current year to second)
 where zflag_name = "regen_expressionmart" ;

update warehouse_run_tracking
 set wrt_last_loaded_date = current year to second
 where wrt_mart_name = "expression mart";