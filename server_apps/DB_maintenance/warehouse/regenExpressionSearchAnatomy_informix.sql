begin work;

DROP TABLE IF EXISTS expression_search_anatomy_generated;
DROP TABLE IF EXISTS expression_search_anatomy_generated_temp;
DROP TABLE IF EXISTS tmp_efs_map;
DROP TABLE IF EXISTS tmp_all_term_contains;

CREATE TABLE expression_search_anatomy_generated_temp (
  esagt_efs_id varchar(100),
  esagt_term_name varchar(255),
  esagt_is_found boolean,
  esagt_is_direct boolean,
  esagt_term_zdb_id varchar(50),
  esagt_distance int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
extent size 819200  next size 819200;

CREATE TABLE expression_search_anatomy_generated (
  esag_efs_id varchar(100),
  esag_term_name varchar(255),
  esag_is_direct boolean,
  esag_term_zdb_id varchar(50),
  esag_distance int
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 819200  next size 819200;


create table tmp_efs_map (efs1 int8, 
       	    	  	      	    efs2 int8, 
				    xpat_found boolean, 
       	    	  	      	    xpatres_superterm varchar(50), 
				    xpatres_subterm varchar(50)
) fragment by round robin in tbldbs1, tbldbs2, tbldbs3
 extent size 819200  next size 819200;


create table tmp_all_term_contains 
  (
    alltermcon_container_zdb_id varchar(50),
    alltermcon_contained_zdb_id varchar(50),
    alltermcon_min_contain_distance integer not null 
  ) fragment by round robin in tbldbs1, tbldbs2, tbldbs3 
  extent size 819200  next size 819200;

create index alltermcon_contained_zdb_id_index_t on 
    tmp_all_term_contains (alltermcon_contained_zdb_id) 
    using btree  in idxdbs2;

create index alltermcon_container_zdb_id_index_t on 
    tmp_all_term_contains (alltermcon_container_zdb_id) 
    using btree  in idxdbs2;

insert into tmp_all_term_contains
  select all_term_contains.* from all_Term_contains, term
    where alltermcon_container_zdb_id = term_zdb_id
 and term_ontology = 'zebrafish_anatomy';


commit work;

begin work;

set pdqpriority high;

--set explain on avoid_execute;

insert into tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
  SELECT DISTINCT efs1.efs_pk_id as efs1, 
 		 efs2.efs_pk_id as efs2, 
		 er.xpatres_expression_found as xpat_found,
 		 er.xpatres_superterm_zdb_id as xpatres_superterm, 
		 er.xpatres_subterm_zdb_id as xpatres_subterm
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
        AND efs2.efs_pk_id = er.xpatres_efs_id      ;          -- get the anatomy term results of the second experiment


create index tmp_index on tmp_efs_map (efs2) using btree in idxdbs3;
create index tmp_index_x on tmp_efs_map (xpatres_superterm) using btree in idxdbs1;
create index tmp_index_x2 on tmp_efs_map (xpatres_subterm) using btree in idxdbs2;

update statistics high for table tmp_all_term_contains;
update statistics high for table tmp_efs_map;

--set explain on avoid_execute; 

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT  distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT 
    'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_subterm = atc.alltermcon_contained_zdb_id
    and xpatres_subterm is not null;


delete from tmp_efs_map;

commit work ;

begin work;

set pdqpriority high;

INSERT INTO tmp_efs_map(efs1, efs2, xpat_found, xpatres_superterm, xpatres_subterm)
  SELECT distinct
  	 efs1.efs_pk_id as efs1, 
  	 efs2.efs_pk_id as efs2, 
	 er.xpatres_expression_found as xpat_found, 
  	 er.xpatres_superterm_zdb_id as xpatres_superterm, 
	 er.xpatres_subterm_zdb_id as xpatres_subterm
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
        AND xpatex2.xpatex_gene_zdb_id IS NOT NULL      
	AND xpatex2.xpatex_genox_zdb_id = xpatex1.xpatex_genox_zdb_id            -- experiments must be about a gene and the same gene
        AND xpatex2.xpatex_gene_zdb_id = xpatex1.xpatex_gene_zdb_id
        AND genox1.genox_is_std_or_generic_control = 't'             -- they're both some kind of wildtype experiment
        AND genox2.genox_is_std_or_generic_control = 't'
        AND fish1.fish_is_wildtype = 't'
        AND fish2.fish_is_wildtype = 't'
        AND fish1.fish_functional_affected_gene_count = 0
        AND fish2.fish_functional_affected_gene_count = 0
        AND efs2.efs_pk_id = er.xpatres_efs_id   ;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 0;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 1;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 2;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 3;



INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 4;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 5;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 6;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 7;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 8;


INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 9;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id,  esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_superterm = atc.alltermcon_contained_zdb_id
  and atc.alltermcon_min_contain_distance = 10;

INSERT INTO expression_search_anatomy_generated_temp (esagt_efs_id, esagt_distance, esagt_is_found, esagt_term_zdb_id)
  SELECT distinct 
     'xpatex-'||efs1 as efs1,
    atc.alltermcon_min_contain_distance,
    xpat_found,
    atc.alltermcon_container_zdb_id
  FROM
    tmp_efs_map,
    tmp_all_term_contains atc
  WHERE xpatres_subterm = atc.alltermcon_contained_zdb_id
    and xpatres_subterm is not null;

create index esagt_efs_index on expression_search_anatomy_generated_temp(esagt_efs_id)
using btree in idxdbs1;

create index esagt_term_index on expression_search_anatomy_generated_temp(esagt_term_zdb_id)
using btree in idxdbs2;

update statistics high for table expression_search_anatomy_generated_temp;

update expression_search_anatomy_generated_temp
  set esagt_is_direct = 't'
 where esagt_distance = 0;

commit work;

begin work;

set pdqpriority high;

INSERT INTO expression_search_anatomy_generated (esag_efs_id, esag_term_zdb_id, esag_is_direct, esag_distance)
SELECT DISTINCT esagt_efs_id, esagt_term_zdb_id, esagt_is_direct, esagt_distance
FROM expression_search_anatomy_generated_temp;

CREATE INDEX esag_efs_id_index
  ON expression_search_anatomy_generated (esag_efs_id);

CREATE INDEX esag_term_zdb_id_index
  ON expression_search_anatomy_generated (esag_term_zdb_id);

commit work;

begin work; 

set pdqpriority high;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 0;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 1;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 2;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 3;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 4;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 5;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 6;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 7;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 8;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 9;

update expression_search_anatomy_generated
  set esag_term_name = (Select term_name from term where term_zdb_id = esag_term_zdb_id)
   where esag_distance = 10;

commit work;
