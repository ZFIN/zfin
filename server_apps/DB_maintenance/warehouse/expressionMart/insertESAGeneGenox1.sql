INSERT INTO tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
  SELECT DISTINCT efs1.efs_pk_id AS efs1, 
 		 efs2.efs_pk_id AS efs2, 
		 er.xpatres_expression_found AS xpat_found,
 		 er.xpatres_superterm_zdb_id AS xpatres_superterm, 
		 er.xpatres_subterm_zdb_id AS xpatres_subterm
  FROM
    expression_experiment2 xpatex1,
    expression_figure_stage efs1,
    expression_experiment2 xpatex2,
    expression_figure_stage efs2,
    expression_result2 er
  WHERE xpatex1.xpatex_zdb_id = efs1.efs_xpatex_zdb_id    -- expand first expression experiment
    AND xpatex2.xpatex_zdb_id = efs2.efs_xpatex_zdb_id    -- expand second expression experiment
    AND xpatex2.xpatex_gene_zdb_id IS NOT NULL                  -- experiments must be about a gene and the same gene
    AND xpatex2.xpatex_gene_zdb_id = xpatex1.xpatex_gene_zdb_id
    AND xpatex2.xpatex_genox_zdb_id = xpatex1.xpatex_genox_zdb_id  -- experiments are about the same fish experiment
    AND efs2.efs_pk_id = er.xpatres_efs_id;          -- get the anatomy term results of the second experiment

DELETE FROM tmp_efs_map
  WHERE xpat_found = 'f';

CREATE index tmp_index on tmp_efs_map (efs2) USING btree IN idxdbs3;
CREATE index tmp_index_x on tmp_efs_map (xpatres_superterm) USING btree IN idxdbs1;
CREATE index tmp_index_x2 on tmp_efs_map (xpatres_subterm) USING btree IN idxdbs2;

UPDATE statistics high for table tmp_all_term_contains;
UPDATE statistics high for table tmp_efs_map;

--SET explain on avoid_execute; 

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_term_zdb_id)
  SELECT 
    'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_subterm = atc.alltermcon_contained_zdb_id
    AND xpatres_subterm IS NOT NULL;


DELETE FROM tmp_efs_map;
