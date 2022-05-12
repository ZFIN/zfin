create view temp_view as
select child.term_name || ' => ' || parent.term_name
from term parent,
     all_term_contains,
     term child
where parent.term_zdb_id = alltermcon_container_zdb_id
  and child.term_zdb_id = alltermcon_contained_zdb_id
  and parent.term_ontology = 'zebrafish_anatomy'
  and child.term_ontology = 'zebrafish_anatomy';

\copy (select * from temp_view) to '@TARGETROOT@/server_apps/solr/prototype/conf/all-term-contains-synonyms.txt' delimiter '|';

drop view temp_view;

create view temp_view as
select parent.term_name || ' => ' || child.term_name
from term parent,
     all_term_contains,
     term child
where parent.term_zdb_id = alltermcon_container_zdb_id
  and child.term_zdb_id = alltermcon_contained_zdb_id
  and parent.term_ontology = 'zebrafish_anatomy'
  and child.term_ontology = 'zebrafish_anatomy';

\copy (select * from temp_view) to '@TARGETROOT@/server_apps/solr/prototype/conf/all-term-contains-synonyms-reversed.txt' delimiter '|';

drop view temp_view;
