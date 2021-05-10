INSERT INTO tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
  SELECT distinct
  	 efs1.efs_pk_id AS efs1, 
  	 efs2.efs_pk_id AS efs2, 
	 er.xpatres_expression_found AS xpat_found, 
  	 er.xpatres_superterm_zdb_id AS xpatres_superterm, 
	 er.xpatres_subterm_zdb_id AS xpatres_subterm
   FROM
    expression_experiment2 xpatex1,
    expression_figure_stage efs1,
    fish_experiment genox1,
    fish fish1,
    expression_experiment2 xpatex2,
    expression_figure_stage efs2,
    fish_experiment genox2,
    fish fish2,
    expression_result2 er
  WHERE xpatex1.xpatex_zdb_id = efs1.efs_xpatex_zdb_id    -- expand first expression experiment
    AND xpatex1.xpatex_genox_zdb_id = genox1.genox_zdb_id
    AND genox1.genox_fish_zdb_id = fish1.fish_zdb_id
    AND xpatex2.xpatex_zdb_id = efs2.efs_xpatex_zdb_id    -- expand second expression experiment
    AND xpatex2.xpatex_genox_zdb_id = genox2.genox_zdb_id
    AND genox2.genox_fish_zdb_id = fish2.fish_zdb_id
    AND xpatex2.xpatex_gene_zdb_id IS NOT NULL                   -- experiments must be about a gene and the same gene
    AND xpatex2.xpatex_gene_zdb_id = xpatex1.xpatex_gene_zdb_id
    AND genox1.genox_is_std_or_generic_control = 't'             -- they're both some kind of wildtype experiment
    AND genox2.genox_is_std_or_generic_control = 't'
    AND fish1.fish_is_wildtype = 't'
    AND fish2.fish_is_wildtype = 't'
    AND fish1.fish_functional_affected_gene_count = 0
    AND fish2.fish_functional_affected_gene_count = 0
    AND efs2.efs_pk_id = er.xpatres_efs_id   ;

DELETE FROM tmp_efs_map
  WHERE xpat_found = 'f';
