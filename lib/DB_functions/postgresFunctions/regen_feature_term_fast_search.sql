----------------------------------------------------------------
-- This function populates feature_stats table
-- Need to store the xpates_zdb_id in the table as well in order to be able
-- to remove specific records. It could be that two xpatres_zdb_id records refer to
-- the same structure and fig.
-------------------------------------------------------------

-- Estimated Run Time:  4 minutes using current clones and antibodies. This time will go up.


-- ---------------------------------------------------------------------
-- Regenerate the antibody_stats table that contains all zdb IDs needed to
-- create a antibody section on the AO detail page:
-- 1) antibody ID or probe ID or ...
-- 2) gene ID
-- 3) superterm ID
-- 4) subterm ID
-- 5) figure ID
-- 6) publication ID
-- 7) xpatres ID
-- 8) type
-- It's like a fact table in a data warehouse
-- ---------------------------------------------------------------------


create or replace function regen_feature_term_fast_search()
returns text as $returnValue$

   -- routine specific variables
  declare featureZdbId  feature.feature_zdb_id%TYPE;
    supertermZdbId  term.term_zdb_id%TYPE;
    subtermZdbId    term.term_zdb_id%TYPE;
    geneZdbId       marker.mrkr_zdb_id%TYPE;
    figureZdbId     figure.fig_zdb_id%TYPE;
    pubZdbId        publication.zdb_id%TYPE;
    imgZdbId        image.img_zdb_id%TYPE;
    xpatresZdbId    expression_result2.xpatres_pk_id%TYPE;
    errorHint text;
  begin 

    errorHint = 'drop existing temp tables';
    -- drop the table if it already exists
    
    drop table if exists feature_stats_temp;

    drop table if exists feature_stats_working;

    drop table if exists feature_stats_old;

    errorHint = 'create feature_stats_temp';
    create table feature_stats_temp (
		fstat_pk_id serial8 not null,
		fstat_feat_zdb_id text not null,
		fstat_superterm_zdb_id text not null,
		fstat_subterm_zdb_id text not null,
		fstat_gene_zdb_id text,
		fstat_fig_zdb_id text not null,
		fstat_pub_zdb_id text  not null,
		fstat_xpatres_zdb_id int8 not null,
		fstat_type varchar(20),
		fstat_img_zdb_id text
	) ;
   

       errorHint = 'insert into feature_stats_temp';
      insert into feature_stats_temp (fstat_pk_id, 
       	      	   		       fstat_feat_zdb_id,
				       fstat_superterm_zdb_id,
				       fstat_subterm_zdb_id,
				       fstat_gene_zdb_id,
				       fstat_fig_zdb_id,
				       fstat_pub_zdb_id,
				       fstat_xpatres_zdb_id,
				       fstat_type,
				       fstat_img_zdb_id)
          select fstat_pk_id, 
       	      	   		       fstat_feat_zdb_id,
				       fstat_superterm_zdb_id,
				       fstat_subterm_zdb_id,
				       fstat_gene_zdb_id,
				       fstat_fig_zdb_id,
				       fstat_pub_zdb_id,
				       fstat_xpatres_zdb_id,
				       fstat_type,
				       fstat_img_zdb_id
	    from feature_stats;

	     errorHint = 'fstat_feat_fk_index';
	    
   	    drop index if exists fstat_feat_fk_index;

	     errorHint = 'fstat_fig_fk_index';

   	    drop index if exists fstat_fig_fk_index;

	     errorHint = 'fstat_gene_fk_index';

  	   
	    drop index if exists fstat_gene_fk_index;

	     errorHint = 'fstat_img_fk_index';

	    drop index if exists fstat_img_fk_index;
	  
	     errorHint = 'fstat_pk_id_index';

	    
	    drop index if exists fstat_pk_id_index;

	     errorHint = 'fstat_pub_fk_index';

	    drop index if exists fstat_pub_fk_index;
	  
	     errorHint = 'fstat_subterm_fk_index';
  	    
	    drop index if exists fstat_subterm_fk_index;

	     errorHint = 'fstat_superterm_fk_index';
  	  
	    drop index if exists fstat_superterm_fk_index;

	    create index fstat_feat_fk_index on feature_stats_temp
    	    	   (fstat_feat_zdb_id);

	    create index fstat_fig_fk_index on feature_stats_temp 
       	           (fstat_fig_zdb_id);

            create index fstat_gene_fk_index on feature_stats_temp
    	    	   (fstat_gene_zdb_id);

            create index fstat_img_fk_index on feature_stats_temp
    	    	   (fstat_img_zdb_id) ;

            create unique index fstat_pk_id_index on
    	    	   feature_stats_temp (fstat_pk_id);

            create index fstat_pub_fk_index on feature_stats_temp 
    	    	   (fstat_pub_zdb_id);

            create index fstat_subterm_fk_index on feature_stats_temp 
    	    	   (fstat_subterm_zdb_id);

            create index fstat_superterm_fk_index on 
    	    	   feature_stats_temp (fstat_superterm_zdb_id);

       errorHint = 'rename feature_stats_temp to feature_stats';
      alter table  feature_stats rename to feature_stats_working;
      alter table feature_stats_temp rename to feature_stats ;

      delete from feature_stats_working;

  
       errorHint = 'insert records for xpatres_superterm_zdb_id';		
      insert into feature_stats_working( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, 
		       xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_pk_id,
		       'Antibody' 
		from antibody, fish_experiment, expression_experiment2, expression_result2, 
			  figure, expression_figure_stage, genotype, all_term_contains, fish
		where  xpatres_expression_found = 't'
		and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		   and efs_xpatex_zdb_id = xpatex_zdb_id
		and fig_zdb_id = efs_fig_zdb_id
		and efs_pk_id = xpatres_efs_id
		and genox_is_std_or_generic_control = 't'
		and fish_zdb_id = genox_fish_zdb_id
		and fish_genotype_zdb_id = geno_zdb_id
		and fish_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055' ;


       errorHint = 'Antibodies: insert records for xpatres_subterm_zdb_id';
	-- Antibodies: insert records for xpatres_subterm_zdb_id

	insert into feature_stats_working( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, 
		       xpatex_source_zdb_id, xpatres_pk_id, 'Antibody' 
		from antibody, fish_experiment, expression_experiment2, expression_result2, 
			  figure, expression_figure_stage, genotype, all_term_contains, fish
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and fig_zdb_id = efs_fig_zdb_id
		   and efs_xpatex_zdb_id = xpatex_zdb_id
		and xpatres_efs_id = efs_pk_id
		and genox_is_std_or_generic_control = 't'
		and fish_zdb_id = genox_fish_zdb_id
		and fish_genotype_zdb_id = geno_zdb_id
		and fish_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
                and xpatres_subterm_zdb_id is not null ;

	 errorHint = 'High-Quality-Probes: insert records for xpatres_superterm_zdb_id';

	-- High-Quality-Probes: insert records for xpatres_superterm_zdb_id
	insert into feature_stats_working (fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_img_zdb_id,
		       fstat_type) 
		select xpatex_probe_feature_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, xpatex_gene_zdb_id, 
		       efs_fig_zdb_id, xpatex_source_zdb_id, xpatres_pk_id, img_zdb_id, 'High-Quality-Probe'
		  from clone, fish, fish_experiment, expression_experiment2, expression_result2, image, 
			  expression_figure_stage, genotype, all_term_contains
		   where  xpatres_expression_found = 't'
	           and genox_zdb_id = xpatex_genox_zdb_id
		   and genox_is_std_or_generic_control = 't'
		   and img_fig_zdb_id = efs_fig_zdb_id
		   and efs_pk_id = xpatres_efs_id
		   and fish_zdb_id = genox_fish_zdb_id
		   and efs_xpatex_zdb_id = xpatex_zdb_id
		   and fish_genotype_zdb_id = geno_zdb_id
		   and fish_is_wildtype = 't'
		   and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		   and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055'
		   and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		   and clone_rating = '4' ;

	 errorHint = 'High-Quality-Probes: insert records for xpatres_subterm_zdb_id';
	-- High-Quality-Probes: insert records for xpatres_subterm_zdb_id
	insert into feature_stats_working (fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_img_zdb_id,
		       fstat_type)	
		select clone_mrkr_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, 
		       efs_fig_zdb_id, xpatex_source_zdb_id, xpatres_pk_id, img_zdb_id, 'High-Quality-Probe'
		from clone, fish,fish_experiment, expression_experiment2, expression_result2, image,
			   expression_figure_stage, genotype, all_term_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and img_fig_zdb_id = efs_fig_zdb_id
		and xpatres_efs_id = efs_pk_id
		   and efs_xpatex_zdb_id = xpatex_zdb_id
		   and genox_is_std_or_generic_control = 't'
		and fish_zdb_id = genox_fish_zdb_id
		and fish_genotype_zdb_id = geno_Zdb_id
    		and fish_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
		and xpatres_subterm_zdb_id !='ZDB-TERM-100331-1055'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		and clone_rating = '4'
                and xpatres_subterm_zdb_id is not null
		and xpatex_probe_feature_zdb_id is not null;

          errorHint = 'rename table feature_stats_new';

	 alter  table feature_stats rename to feature_stats_old;
  	 alter table feature_stats_working rename to feature_stats;
        
	  errorHint = 'success';

  return errorHint;
 
  exception when raise_exception then
  	    return errorHint;    
  end ;
$returnValue$ LANGUAGE plpgsql;

