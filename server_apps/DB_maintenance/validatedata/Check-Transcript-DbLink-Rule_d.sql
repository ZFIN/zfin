SELECT tscript_mrkr_zdb_id
FROM   transcript
WHERE  NOT EXISTS (SELECT 'x'
                   FROM   db_link
                   WHERE  dblink_linked_recid = tscript_mrkr_zdb_id);