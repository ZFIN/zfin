INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 0;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 1;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 2;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 3;



INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 4;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 5;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 6;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 7;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 8;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 9;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_term_zdb_id)
  SELECT DISTINCT
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  AND atc.alltermcon_min_contain_distance = 10;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_term_zdb_id)
  SELECT distinct 
     'xpatex-'||efs1 AS efs1,
    atc.alltermcon_min_contain_distance,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_subterm = atc.alltermcon_contained_zdb_id
    AND xpatres_subterm IS NOT NULL;

CREATE index esagt_efs_index on expression_search_anatomy_generated_temp(esagt_efs_id)
USING btree IN idxdbs1;

CREATE index esagt_term_index ON expression_search_anatomy_generated_temp(esagt_term_zdb_id)
USING btree IN idxdbs2;

UPDATE statistics high FOR TABLE expression_search_anatomy_generated_temp;

UPDATE expression_search_anatomy_generated_temp
  SET esagt_is_direct = 't'
 where esagt_distance = 0;
