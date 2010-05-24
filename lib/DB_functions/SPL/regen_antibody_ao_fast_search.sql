----------------------------------------------------------------
-- This function populates feature_stats table
-- Need to store the xpates_zdb_id in the table as well in order to be able
-- to remove specific records. It could be that two xpatres_zdb_id recoreds refer to
-- the same structure and fig.
-- Do not include
--
-- INPUT VARS:
--             none
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
--              This table is currently used on the AO detail page to obtain the
--		antibodies labeling the ao structure
-------------------------------------------------------------

-- Estimated Run Time: 10-15 seconds with ~300 antibodies in the system. This time will go up.


-- ---------------------------------------------------------------------
-- Regenerate the feature_stats table that contains all zdb IDs needed to
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


CREATE procedure regen_feature_ao_fast_search()
	returning varchar;

    define featureZdbId    lvarchar;
    define supertermZdbId       lvarchar;
    define subtermZdbId       lvarchar;
    define geneZdbId    lvarchar;
    define figureZdbId    lvarchar;
    define pubZdbId       lvarchar;
    define xpatresZdbId       lvarchar;


begin

    -- drop the table if it already exists
    if (exists (select *
	           from systables
		   where tabname = "feature_stats_temp")) then
		drop table feature_stats_temp;
    end if

	create table feature_stats_temp (
		feat_zdb_id varchar(50),
		superterm_zdb_id varchar(50),
		subterm_zdb_id varchar(50),
		gene_zdb_id varchar(50),
		fig_zdb_id varchar(50),
		pub_zdb_id varchar(50),
		xpatres_zdb_id varchar(50),
		type varchar(20),
		foreign key (xpatres_zdb_id) references expression_result
		ON DELETE CASCADE 
	) ;
	

end

	-- Antibodies: insert records for xpatres_superterm_zdb_id
	foreach
	
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id 
			into featureZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId
		from antibody, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_terms_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055'

		insert into feature_stats_temp 
			values (featureZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'Antibody');
	
	end foreach

	-- Antibodies: insert records for xpatres_subterm_zdb_id
	foreach
	
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id 
			into featureZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId
		from antibody, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_terms_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id

		insert into feature_stats_temp 
			values (featureZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'Antibody');
	
	end foreach
	
	-- High-Quality-Probes: insert records for xpatres_superterm_zdb_id
	foreach
	
		select clone_mrkr_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id 
			into featureZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId
		from clone, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_terms_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		and clone_rating = '4' 

		insert into feature_stats_temp 
			values (featureZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'High-Quality-Probe');
	
	end foreach

	-- High-Quality-Probes: insert records for xpatres_subterm_zdb_id
	foreach
	
		select clone_mrkr_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id 
			into featureZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId
		from clone, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_terms_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
		and xpatres_subterm_zdb_id !='ZDB-TERM-100331-1055'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		and clone_rating = '4' 

		insert into feature_stats_temp 
			values (featureZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'High-Quality-Probe');
	
	end foreach

	

    if (exists (select *
	           from systables
		   where tabname = "feature_stats")) then
		drop table feature_stats;
    end if

	rename table feature_stats_temp to feature_stats;

    create index feature_stats_zdb_id on feature_stats (atb_zdb_id);
    create index superterm_stats_zdb_id on feature_stats (superterm_zdb_id);
    create index subterm_stats_zdb_id on feature_stats (subterm_zdb_id);
    create index gene_stats_zdb_id on feature_stats (gene_zdb_id);

end procedure
