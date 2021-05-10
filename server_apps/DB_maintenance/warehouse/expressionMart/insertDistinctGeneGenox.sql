INSERT INTO tmp_esag_predistinct (esag_efs_id, esag_term_zdb_id, esag_is_direct, esag_distance)
SELECT DISTINCT esagt_efs_id, esagt_term_zdb_id, esagt_is_direct, esagt_distance
FROM expression_search_anatomy_generated_temp;

CREATE INDEX tmp_efs_id_index
  ON tmp_esag_predistinct (esag_efs_id);

CREATE INDEX tmp_term_zdb_id_index
  ON tmp_esag_predistinct (esag_term_zdb_id);
