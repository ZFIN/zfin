SELECT distinct (dblink_acc_num), mrkr_zdb_id
FROM   marker
  JOIN (SELECT Count(*)            AS be_zero,
               dblink_linked_recid AS troublemaker_zdb_id
        FROM   db_link dbl1
        WHERE  dblink_acc_num LIKE 'ENSDARG%'
          -- use only GRCz11
          AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
        GROUP  BY dblink_linked_recid
        HAVING Count(*) > 1) as query
    ON mrkr_zdb_id = troublemaker_zdb_id
  JOIN db_link
    ON dblink_linked_recid = mrkr_zdb_id
WHERE  dblink_acc_num LIKE 'ENSDARG%';
