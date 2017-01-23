-- File: fixZecoAnnotations.sql
-- In this script annotations using ZECO are updated (experiment_conditions)
-- In particular, any term that has turned into a secondary term is
-- updated with its corresponding primary term, i.e. the term into which
-- the original term was merged into.


-- update zeco terms

------------------------------------------------------
-- Reports for obsolete term usage
------------------------------------------------------
-- report obsoleted zeco term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_zeco_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

-- report obsoleted zeco-taxa term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_taxon_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

-- report obsoleted chebi term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_chebi_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

-- report obsoleted ao term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_ao_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

-- report obsoleted go-cc term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_go_cc_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

-- report obsoleted disease term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   disease_annotation
               WHERE  dat_term_zdb_id = term_zdb_id)
       AND term_is_obsolete = 't';

------------------------------------------------------
-- Reports for secondary term usage
------------------------------------------------------

-- report secondary zeco term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_zeco_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

-- report secondary zeco-taxa term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_taxon_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

-- report secondary chebi term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_chebi_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

-- report secondary ao term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_ao_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

-- report secondary go-cc term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   experiment_condition
               WHERE  expcond_go_cc_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

-- report secondary disease term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       term_comment
FROM   term
WHERE  EXISTS (SELECT 'x'
               FROM   disease_annotation
               WHERE  dat_term_zdb_id = term_zdb_id)
       AND term_is_secondary = 't';

