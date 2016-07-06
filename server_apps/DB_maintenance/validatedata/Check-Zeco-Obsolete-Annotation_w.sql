SELECT expcond_zdb_id,
       term_name
FROM   experiment_condition,
       term
WHERE  expcond_zeco_term_zdb_id= term_zdb_id
       AND term_is_obsolete = "t"
UNION
SELECT expcond_zdb_id,
       term_name
FROM   experiment_condition,
       term
WHERE  expcond_chebi_term_zdb_id= term_zdb_id
       AND term_is_obsolete = "t"
UNION
SELECT expcond_zdb_id,
       term_name
FROM   experiment_condition,
       term
WHERE  expcond_taxon_term_zdb_id= term_zdb_id
       AND term_is_obsolete = "t"
UNION
SELECT expcond_zdb_id,
       term_name
FROM   experiment_condition,
       term
WHERE  expcond_ao_term_zdb_id= term_zdb_id
       AND term_is_obsolete = "t"
UNION
SELECT expcond_zdb_id,
       term_name
FROM   experiment_condition,
       term
WHERE  expcond_go_cc_term_zdb_id= term_zdb_id
       AND term_is_obsolete = "t"
