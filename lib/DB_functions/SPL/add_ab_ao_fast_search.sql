----------------------------------------------------------------
-- This function adds new records to the antibody_stats table
-- this function is called when a new expression_result record is created
-- Need to store the xpatres_zdb_id in the table as well in order to be able
-- to remove specific records. It could be that two xpatres_zdb_id records refer to
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
--		antibodies labeling the ao structure.
-------------------------------------------------------------

CREATE procedure add_ab_ao_fast_search(xpatresZdbId int8)
	returning varchar;

    define atbZdbId    lvarchar;
    define supertermZdbId       lvarchar;
    define subtermZdbId       lvarchar;
    define geneZdbId    lvarchar;
    define figureZdbId    lvarchar;
    define pubZdbId       lvarchar;


begin
    -- insert records for xpatres_superterm_zdb_id
    foreach
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id
			into atbZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId
		from antibody, fish_experiment, expression_experiment2, expression_result2, 
			  figure, expression_figure_stage, genotype, all_term_contains, fish
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_efs_id = efs_pk_id
		and xpatex_zdb_id = efs_xpatex_zdb_id
		and fig_zdb_id = efs_fig_zdb_id
		and efs_pk_id = xpatres_efs_id
		and genox_is_std_or_generic_control = 't'
		and fish_zdb_id = genox_fish_zdb_id
		and fish_genotype_zdb_id = geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_pk_id = xpatresZdbId
		-- not unspecified
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055'

		insert into feature_stats ( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)
			values (atbZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'Antibody');
	
     end foreach

     -- insert records for xpatres_subterm_zdb_id
     foreach
	
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id
			into atbZdbId, supertermZdbId, subtermZdbId, geneZdbId, figureZdbId, pubZdbId
		from antibody, fish_experiment, expression_experiment2, expression_result2, 
			  figure, expression_figure_stage, genotype, all_term_contains, fish
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_efs_id = efs_pk_id
		and xpatex_zdb_id = efs_xpatex_zdb_id
		and fig_zdb_id = efs_fig_zdb_id
		and xpatres_efs_id = efs_pk_id
		and genox_is_std_or_generic_control
		and fish_genotype_zdb_id = geno_zdb_id
		and genox_fish_zdb_id = fish_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
		and xpatres_pk_id = xpatresZdbId

		insert into feature_stats ( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)
			values (atbZdbId, supertermZdbId,subtermZdbId, geneZdbId, figureZdbId, pubZdbId, xpatresZdbId,'Antibody');
	
	end foreach
end
end procedure

