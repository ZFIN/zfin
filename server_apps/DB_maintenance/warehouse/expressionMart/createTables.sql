
CREATE TABLE expression_search_anatomy_generated_temp (
  esagt_efs_id varchar(100),
  esagt_term_name varchar(255),
  esagt_is_direct boolean,
  esagt_term_zdb_id varchar(50),
  esagt_distance int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 819200  next size 819200;

CREATE TABLE tmp_esag_predistinct (
  esag_efs_id varchar(100),
  esag_term_name varchar(255),
  esag_is_direct boolean,
  esag_term_zdb_id varchar(50),
  esag_distance int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 819200  next size 819200;

CREATE TABLE tmp_efs_map (efs1 int8,
                          efs2 int8,
                          xpat_found boolean,
                          xpatres_superterm varchar(50),
                          xpatres_subterm varchar(50)
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 819200  next size 819200;


CREATE TABLE tmp_all_term_contains 
  (
    alltermcon_container_zdb_id varchar(50),
    alltermcon_contained_zdb_id varchar(50),
    alltermcon_min_contain_distance integer NOT NULL 
  ) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
  extent size 819200  next size 819200;

CREATE index alltermcon_contained_zdb_id_index_t ON 
    tmp_all_term_contains (alltermcon_contained_zdb_id) 
    USING btree  in idxdbs2;

CREATE index alltermcon_container_zdb_id_index_t ON 
    tmp_all_term_contains (alltermcon_container_zdb_id) 
    USING btree  in idxdbs2;

INSERT into tmp_all_term_contains
  SELECT all_term_contains.* FROM all_term_contains, term
    WHERE alltermcon_container_zdb_id = term_zdb_id
 AND term_ontology = 'zebrafish_anatomy';

