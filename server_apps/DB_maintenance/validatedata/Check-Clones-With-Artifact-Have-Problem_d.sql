SELECT m.mrkr_name,
       m.mrkr_zdb_id,
       m.mrkr_abbrev
FROM   clone c
       JOIN marker_relationship mr
         ON c.clone_mrkr_zdb_id = mr.mrel_mrkr_2_zdb_id
       JOIN marker m
         ON c.clone_mrkr_zdb_id = m.mrkr_zdb_id
WHERE  c.clone_problem_type IS NULL
       AND mr.mrel_type = 'gene has artifact' ;