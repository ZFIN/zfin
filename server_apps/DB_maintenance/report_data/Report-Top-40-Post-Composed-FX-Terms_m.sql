SELECT   termone.term_name,
  termtwo.term_name,
  count(*) AS frequency
FROM     expression_result2,
  term termone,
  term termtwo
WHERE    xpatres_superterm_zdb_id IS NOT NULL
         AND      xpatres_subterm_zdb_id IS NOT NULL
         AND      termone.term_zdb_id = xpatres_superterm_zdb_id
         AND      termtwo.term_zdb_id = xpatres_subterm_zdb_id
GROUP BY termone.term_name,
  termtwo.term_name
ORDER BY frequency DESC,
  termone.term_name
LIMIT 40;