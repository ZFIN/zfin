SELECT distinct(mrkr_zdb_id),
  dbl1.dblink_acc_num
FROM   marker
  JOIN db_link dbl1
    ON mrkr_zdb_id = dbl1.dblink_linked_recid
  JOIN (SELECT Count(*) AS be_zero,
          dblink_acc_num
        FROM   db_link
        WHERE  dblink_acc_num LIKE 'ENSDARG%'
        -- use only GRCz11
        AND dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
        GROUP  BY dblink_acc_num
        HAVING Count(*) > 1) dbl2
    ON dbl1.dblink_acc_num = dbl2.dblink_acc_num