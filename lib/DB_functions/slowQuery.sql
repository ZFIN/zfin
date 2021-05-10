begin work;

   create table feature_stats_working (
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
   

explain
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
		and clone_rating = '4';

rollback work;
