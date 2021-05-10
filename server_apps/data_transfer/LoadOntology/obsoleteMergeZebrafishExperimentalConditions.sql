-- report obsoleted zeco term usage
unload to 'obsoleted_terms'
SELECT exp_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term,
       experiment_condition,
       experiment
WHERE  expcond_zeco_term_zdb_id = term_zdb_id
       AND exp_zdb_id = expcond_exp_zdb_id
       AND term_is_obsolete = 't';

-- report secondary zeco term usage
unload to 'merged_terms'
SELECT exp_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term,
       experiment_condition,
       experiment
WHERE  expcond_zeco_term_zdb_id = term_zdb_id
       AND exp_zdb_id = expcond_exp_zdb_id
       AND term_is_secondary = 't';
