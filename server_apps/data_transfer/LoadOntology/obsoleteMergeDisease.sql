-- report obsoleted disease term usage
unload to 'obsoleted_terms'
SELECT dat_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term,
       disease_annotation
WHERE  dat_term_zdb_id = term_zdb_id
       AND term_is_obsolete = 't';

-- report secondary disease term usage
unload to 'merged_terms'
SELECT dat_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term,
       disease_annotation
WHERE  dat_term_zdb_id = term_zdb_id
       AND term_is_secondary = 't';

