unload to 'all-term-contains-synonyms.txt' 
select child.term_name || ' => ' || parent.term_name 
from term parent, all_term_contains, term child 
where parent.term_zdb_id = alltermcon_container_zdb_id
  and child.term_zdb_id = alltermcon_contained_zdb_id
  and parent.term_ontology = 'zebrafish_anatomy'
  and child.term_ontology = 'zebrafish_anatomy';
--  and alltermcon_min_contain_distance > 1
--  and alltermcon_min_contain_distance <= 4;


unload to 'all-term-contains-synonyms-reversed.txt'
select parent.term_name || ' => ' || child.term_name
from term parent, all_term_contains, term child
where parent.term_zdb_id = alltermcon_container_zdb_id
  and child.term_zdb_id = alltermcon_contained_zdb_id
  and parent.term_ontology = 'zebrafish_anatomy'
  and child.term_ontology = 'zebrafish_anatomy';
--  and alltermcon_min_contain_distance > 1
--  and alltermcon_min_contain_distance <= 4;



-- remove the | delimiters and replace , with \,
!/private/bin/perl -p -i -e 's/\|//' all-term-contains-synonyms.txt
!/private/bin/perl -p -i -e 's/\|//' all-term-contains-synonyms-reversed.txt
-- !/private/bin/perl -p -i -e 's/,/\\,/' all-term-contains-synonyms.txt


