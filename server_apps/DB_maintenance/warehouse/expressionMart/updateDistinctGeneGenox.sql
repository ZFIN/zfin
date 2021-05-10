UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 0;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 1;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 2;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 3;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 4;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 5;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 6;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 7;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 8;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 9;

UPDATE tmp_esag_predistinct
  SET esag_term_name = (SELECT term_name FROM term WHERE term_zdb_id = esag_term_zdb_id)
   WHERE esag_distance = 10;
