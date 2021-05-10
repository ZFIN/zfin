copy (
select child.term_name || ' => ' || parent.term_name
from term parent, all_term_contains, term child
where parent.term_zdb_id = alltermcon_container_zdb_id
      and child.term_zdb_id = alltermcon_contained_zdb_id
      and parent.term_ontology = 'zebrafish_anatomy'
      and child.term_ontology = 'zebrafish_anatomy' ) to '@TARGETROOT@/server_apps/solr/prototype/conf/all-term-contains-synonyms.txt' delimiter '|'
--  and alltermcon_min_contain_distance > 1
--  and alltermcon_min_contain_distance <= 4;
;


copy (
select parent.term_name || ' => ' || child.term_name
from term parent, all_term_contains, term child
where parent.term_zdb_id = alltermcon_container_zdb_id
      and child.term_zdb_id = alltermcon_contained_zdb_id
      and parent.term_ontology = 'zebrafish_anatomy'
      and child.term_ontology = 'zebrafish_anatomy' ) to '@TARGETROOT@/server_apps/solr/prototype/conf/all-term-contains-synonyms-reversed.txt' delimiter '|'
--  and alltermcon_min_contain_distance > 1
--  and alltermcon_min_contain_distance <= 4;
;
