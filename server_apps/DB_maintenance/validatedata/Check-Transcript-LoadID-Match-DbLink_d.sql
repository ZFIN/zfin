SELECT tscript_mrkr_zdb_id,
       tscript_load_id,
       dblink_acc_num
FROM   transcript,
       db_link
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   db_link b
                   WHERE  b.dblink_linked_recid = tscript_mrkr_zdb_id
                          AND b.dblink_acc_num = tscript_load_id)
       AND tscript_load_id != tscript_mrkr_zdb_id
       AND dblink_linked_recid = tscript_mrkr_zdb_id
       AND NOT EXISTS (SELECT 'x'
                       FROM   data_alias
                       WHERE  dalias_data_zdb_id = tscript_mrkr_zdb_id
                              AND dalias_alias LIKE 'OTTDAR%');