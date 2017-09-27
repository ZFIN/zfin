-- report obsoleted disease term usage
unload to 'obsoleted_disease_terms'
SELECT dat_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       CASE
         WHEN damo_genox_zdb_id IS NOT NULL THEN (SELECT fish_handle
                                                  FROM   fish_experiment,
                                                         fish
                                                  WHERE
         genox_zdb_id = damo_genox_zdb_id
         AND genox_fish_zdb_id = fish_zdb_id)
         ELSE '--'
       END
FROM   term
       join disease_annotation on dat_term_zdb_id = term_zdb_id
       left outer join disease_annotation_model on damo_dat_zdb_id = dat_zdb_id
WHERE  term_is_obsolete = 't';

-- report secondary disease term usage
unload to 'merged_disease_terms'
SELECT dat_source_zdb_id,
       term_zdb_id,
       term_ont_id,
       term_name,
       CASE
         WHEN damo_genox_zdb_id IS NOT NULL THEN (SELECT fish_handle
                                                  FROM   fish_experiment,
                                                         fish
                                                  WHERE
         genox_zdb_id = damo_genox_zdb_id
         AND genox_fish_zdb_id = fish_zdb_id)
         ELSE '--'
       END
FROM   term
       join disease_annotation on dat_term_zdb_id = term_zdb_id
       left outer join disease_annotation_model on damo_dat_zdb_id = dat_zdb_id
WHERE  term_is_secondary= 't';


