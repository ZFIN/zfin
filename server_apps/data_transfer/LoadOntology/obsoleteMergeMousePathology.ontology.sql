-- report obsoleted PATO term usage
unload to 'obsoleted_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       xpatex_source_zdb_id
FROM   term
       join phenotype_statement on phenos_quality_zdb_id = term_zdb_id
       join phenotype_experiment on phenos_Phenox_pk_id = phenox_pk_id
       join fish_experiment on phenox_genox_zdb_id = genox_zdb_id
       join expression_experiment on xpatex_genox_zdb_id = genox_zdb_id
WHERE  term_is_obsolete = 't';


-- report secondary PATO term usage
unload to 'merged_terms'
SELECT term_zdb_id,
       term_ont_id,
       term_name,
       xpatex_source_zdb_id
FROM   term
       join phenotype_statement on phenos_quality_zdb_id = term_zdb_id
       join phenotype_experiment on phenos_Phenox_pk_id = phenox_pk_id
       join fish_experiment on phenox_genox_zdb_id = genox_zdb_id
       join expression_experiment on xpatex_genox_zdb_id = genox_zdb_id
WHERE  term_is_secondary = 't';
