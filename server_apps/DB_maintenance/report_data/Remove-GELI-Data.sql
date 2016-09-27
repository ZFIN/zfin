SELECT fig_zdb_id,
  fig_comments,
  fig_source_zdb_id
FROM   figure
WHERE  fig_source_zdb_id = '$PUBID'
       AND fig_comments = 'GELI';

-- delete expression_experiment records via cascade for GELI records
DELETE FROM zdb_active_data
WHERE  zactvd_zdb_id IN (SELECT xpatex_zdb_id
                         FROM   expression_experiment2,
                                figure
                         WHERE  xpatex_source_zdb_id = '$PUBID'
                                AND fig_source_zdb_id = xpatex_source_zdb_id
                                AND fig_comments = 'GELI');

-- delete GELI figures via cascade
DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN (SELECT fig_zdb_id
                         FROM   figure
                         WHERE  fig_source_zdb_id = '$PUBID'
                                AND fig_comments = 'GELI');


insert into curation (cur_zdb_id, 
update publication
 set pub_geli_removed = 't'
 where zdb_id = '$PUBID';
