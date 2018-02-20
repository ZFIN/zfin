INSERT INTO expression_search_anatomy_generated (esag_efs_id, esag_term_name, esag_is_direct)
  SELECT DISTINCT esag_efs_id, esag_term_name, esag_is_direct
  FROM tmp_esag_predistinct;

CREATE INDEX esag_efs_id_index
  ON expression_search_anatomy_generated (esag_efs_id);
