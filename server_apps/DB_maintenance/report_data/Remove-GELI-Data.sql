SELECT fig_zdb_id,
  fig_comments,
  fig_source_zdb_id
FROM   figure
WHERE  fig_source_zdb_id = '$PUBID'
       AND fig_comments = 'GELI';

begin work ;

DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN (SELECT fig_zdb_id
                         FROM   figure
                         WHERE  fig_source_zdb_id = '$PUBID'
                                AND fig_comments = 'GELI');

DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN (SELECT xpatex_zdb_id
                         FROM   expression_experiment
                         WHERE  xpatex_source_zdb_id = '$PUBID');

commit work ;