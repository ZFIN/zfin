create or replace function regen_expression_mart_per_pub (vPubZdbId text)
returns text as $regen_expression_mart_per_pub$

declare vResid int8;
        vXpatex text; 
	vStart text;
	 vEnd text; 
	vSuper text; 
	vSub text; 
	vFig text;
        vFound boolean;

begin 

 vResid = (Select max(xpatres_zdb_id) from expression_result);

 delete from expression_pattern_figure
    where exists (Select 'x' from expression_result, expression_experiment
       	      	      where xpatfig_xpatres_zdb_id = xpatres_zdb_id
		      and xpatres_xpatex_zdb_id = xpatex_zdb_id
		      and xpatex_source_zdb_id = vPubZdbId);

  delete from expression_result
    where exists (select 'x' from expression_experiment
       	      	      where xpatres_xpateX_zdb_id = xpatex_zdb_id
		      and xpatex_source_zdb_id = vPubZdbId);


  delete from expression_experiment
    where xpatex_source_zdb_id = vPubZdbId;

  insert into expression_experiment (xpatex_zdb_id,
       	    			   xpatex_assay_name,
				   xpatex_probe_feature_zdb_id,
				   xpatex_gene_zdb_id,
				   xpatex_direct_submission_date,
				   xpatex_dblink_zdb_id,
				   xpatex_genox_zdb_id,
				   xpatex_atb_zdb_id,
				   xpatex_source_zdb_id)
   select distinct xpatex_zdb_id, xpatex_assay_name,
 	xpatex_probe_feature_zdb_id, xpatex_gene_zdb_id,
	xpatex_direct_submission_date, xpatex_dblink_zdb_id,
	xpatex_genox_zdb_id, xpatex_atb_zdb_id, xpatex_source_zdb_id
   from expression_Experiment2
   where xpatex_source_zdb_id = vPubZdbId;

  for vXpatex,vStart,vEnd,vFound,vSuper,vSub,vFig in

   select distinct efs_xpatex_zdb_id, 
  	  	  efs_start_stg_zdb_id, 
	  	  efs_end_stg_zdb_id, 
		  xpatres_expression_found, 
    		  xpatres_superterm_zdb_id,
		  xpatres_subterm_zdb_id,
		  efs_fig_zdb_id
  from expression_result2, expression_figure_stage,expression_experiment2
  where xpatres_efs_id = efs_pk_id
    and xpatex_zdb_id = efs_xpatex_Zdb_id
    and xpatex_source_zdb_id = vPubZdbId  
  loop

  vResid = vResid +1;

  insert into expression_result (xpatres_zdb_id,
				xpatres_xpatex_zdb_id,
				xpatres_start_stg_zdb_id,
				xpatres_end_stg_zdb_id,
				xpatres_expression_found,
				xpatres_superterm_zdb_id,
    				xpatres_subterm_zdb_id,
    				xpatres_fig_zdb_id)
     values (vResid,vXpatex,vStart,vEnd,vFound,vSuper,vSub,vFig);
end loop;

insert into expression_pattern_figure (xpatfig_xpatres_zdb_id, 
       	    			       xpatfig_fig_zdb_id)
  select distinct xpatres_zdb_id,xpatres_fig_zdb_id
    from expression_result, expression_figure_stage, expression_experiment
    where xpatres_xpatex_zdb_id =efs_xpateX_zdb_id
      and xpatres_start_stg_zdb_id = efs_start_stg_zdb_id
      and xpatres_end_stg_zdb_id = efs_end_stg_zdb_id 
      and xpatex_source_zdb_id = vPubZdbId
      and xpatex_zdb_id = xpatres_xpatex_zdb_id;


end ;

$regen_expression_mart_per_pub$ LANGUAGE plpgsql;
