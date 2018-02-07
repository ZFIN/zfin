begin work;

DROP TABLE IF EXISTS expression_search_anatomy_generated;

CREATE TEMP TABLE expression_search_anatomy_generated_temp (
  esagt_efs_id varchar(100),
  esagt_term_name varchar(255),
  esagt_is_found boolean,
  esagt_is_direct boolean,
  esagt_term_zdb_id varchar(50),
  esagt_distance int
) ;

CREATE TABLE expression_search_anatomy_generated (
  esag_efs_id varchar(100),
  esag_term_name varchar(255),
  esag_is_direct boolean
);


CREATE temp table tmp_efs_map (efs1 int8, efs2 int8, xpat_found boolean, xpatres_superterm varchar(50), xpatres_subterm varchar(50))
with no log;

INSERT into tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
 SELECT DISTINCT efs1.efs_pk_id, efs2.efs_pk_id, er.xpatres_expression_found, er.xpatres_superterm_zdb_id, er.xpatres_subterm_zdb_id
   FROM
    expression_experiment2 xpatex1,
    expression_figure_stage efs1,
    expression_experiment2 xpatex2,
    expression_figure_stage efs2,
    expression_result2 er
   WHERE xpatex1.xpatex_zdb_id = efs1.efs_xpatex_zdb_id    -- expAND first expression experiment
        AND xpatex2.xpatex_zdb_id = efs2.efs_xpatex_zdb_id    -- expAND second expression experiment
        AND xpatex2.xpatex_gene_zdb_id = xpatex1.xpatex_gene_zdb_id
        AND xpatex2.xpatex_genox_zdb_id = xpatex1.xpatex_genox_zdb_id  -- experiments are about the same fish experiment
        AND efs2.efs_pk_id = er.xpatres_efs_id      ;          -- get the anatomy term results of the second experiment;


INSERT INTO tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
  SELECT DISTINCT efs1.efs_pk_id, efs2.efs_pk_id, er.xpatres_expression_found, er.xpatres_superterm_zdb_id, er.xpatres_subterm_zdb_id
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
  WHERE xpatex1.xpatex_zdb_id = efs1.efs_xpatex_zdb_id    -- expAND first expression experiment
        AND xpatex1.xpatex_genox_zdb_id = genox1.genox_zdb_id
        AND genox1.genox_fish_zdb_id = fish1.fish_zdb_id
        AND xpatex2.xpatex_zdb_id = efs2.efs_xpatex_zdb_id    -- expAND second expression experiment
        AND xpatex2.xpatex_genox_zdb_id = genox2.genox_zdb_id
        AND genox2.genox_fish_zdb_id = fish2.fish_zdb_id
        AND xpatex2.xpatex_gene_zdb_id IS NOT NULL                  -- experiments must be about a gene AND the same gene
        AND xpatex2.xpatex_gene_zdb_id = xpatex1.xpatex_gene_zdb_id
        AND genox1.genox_is_std_or_generic_control = 't'             -- they're both some kind of wildtype experiment
        AND genox2.genox_is_std_or_generic_control = 't'
        AND fish1.fish_is_wildtype = 't'
        AND fish2.fish_is_wildtype = 't'
        AND fish1.fish_functional_affected_gene_count = 0
        AND fish2.fish_functional_affected_gene_count = 0
        AND efs2.efs_pk_id = er.xpatres_efs_id ;


CREATE index tmp_index on tmp_efs_map (efs2) USING btree in idxdbs3;
CREATE index tmp_index_x on tmp_efs_map (xpatres_superterm) USING btree in idxdbs1;
CREATE index tmp_index_x2 on tmp_efs_map (xpatres_subterm) USING btree in idxdbs2;

CREATE temp table tmp_all_term_contains 
  (
    alltermcon_container_zdb_id varchar(50),
    alltermcon_contained_zdb_id varchar(50),
    alltermcon_min_contain_distance integer not null 
  ) with no log;

CREATE index alltermcon_contained_zdb_id_index_t on 
    tmp_all_term_contains (alltermcon_contained_zdb_id) 
    USING btree  in idxdbs2;

CREATE index alltermcon_container_zdb_id_index_t on 
    tmp_all_term_contains (alltermcon_container_zdb_id) 
    USING btree  in idxdbs2;

INSERT into tmp_all_term_contains
  SELECT all_term_contains.* FROM all_Term_contains, term
    WHERE alltermcon_container_zdb_id = term_zdb_id
 AND term_ontology = 'zebrafish_anatomy';

UPDATE statistics high for table tmp_all_term_contains;

--set explain on avoid_execute; 


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT DISTINCT 
    'xpatex-' || efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT DISTINCT
    'xpatex-' || efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_subterm = atc.alltermcon_contained_zdb_id
    AND xpatres_subterm IS NOT NULL;

UPDATE expression_search_anatomy_generated_temp
  set esagt_is_direct = 't'
 WHERE esagt_distance = 0;

UPDATE expression_search_anatomy_generated_temp
  set esagt_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esagt_term_zdb_id);

INSERT INTO expression_search_anatomy_generated (esag_efs_id, esag_term_name, esag_is_direct)
SELECT DISTINCT esagt_efs_id, esagt_term_name, esagt_is_direct
FROM expression_search_anatomy_generated_temp;

CREATE INDEX esag_efs_id_index
  ON expression_search_anatomy_generated (esag_efs_id);

commit work;
