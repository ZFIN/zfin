INSERT INTO expression_search_anatomy_generated (esag_efs_id, esag_term_name, esag_is_direct)
  SELECT DISTINCT esag_efs_id, esag_term_name, esag_is_direct
  FROM tmp_esag_predistinct;

update statistics high for table expression_search_anatomy_generated;
