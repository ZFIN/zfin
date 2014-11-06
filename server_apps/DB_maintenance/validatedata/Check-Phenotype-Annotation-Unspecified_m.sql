SELECT DISTINCT full_name
  ,fig_source_zdb_id
  ,title
FROM figure fig
INNER JOIN curation cur ON fig.fig_source_zdb_id = cur.cur_pub_zdb_id
INNER JOIN person per ON cur.cur_curator_zdb_id = per.zdb_id
INNER JOIN int_person_lab plab ON plab.source_id = per.zdb_id
INNER JOIN lab_position pos ON plab.position_id = pos.labpos_pk_id
INNER JOIN publication pub on pub.zdb_id = fig.fig_source_zdb_id
WHERE cur.cur_topic = 'Phenotype'
  AND cur.cur_closed_date IS NOT NULL
  AND pub.pub_completion_date IS NOT NULL
  AND fig.fig_zdb_id IN (
    SELECT phenox_fig_zdb_id
    FROM phenotype_experiment
    WHERE NOT EXISTS (
        SELECT *
        FROM phenotype_statement
        WHERE phenos_phenox_pk_id = phenox_pk_id
        )
    )
  AND plab.target_id = 'ZDB-LAB-000914-1'
  AND pos.labpos_position = 'Research Staff'
ORDER BY full_name;