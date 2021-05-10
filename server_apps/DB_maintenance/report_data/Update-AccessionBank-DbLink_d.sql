SELECT dblink_acc_num, 'DB_LINK'
FROM   db_link
WHERE  dblink_length IS NULL
       AND EXISTS (SELECT 'x'
                   FROM   accession_bank
                   WHERE  accbk_acc_num = dblink_acc_num
                          AND accbk_fdbcont_zdb_id = dblink_fdbcont_zdb_id
                          AND accbk_length IS NOT NULL)
UNION
SELECT accbk_acc_num, 'ACCESSION_BANK'
FROM   accession_bank
WHERE  accbk_length IS NULL
       AND EXISTS (SELECT 'x'
                   FROM   db_link
                   WHERE  accbk_acc_num = dblink_acc_num
                          AND accbk_fdbcont_zdb_id = dblink_fdbcont_zdb_id
                          AND dblink_length IS NOT NULL);


-- update DB_LINK table
UPDATE db_link
SET    dblink_length = (SELECT accbk_length
                        FROM   accession_bank
                        WHERE  accbk_acc_num = dblink_acc_num
                               AND dblink_fdbcont_zdb_id = accbk_fdbcont_zdb_id
                               AND accbk_length IS NOT NULL)
WHERE  dblink_length IS NULL
AND EXISTS (SELECT 'x'
                   FROM   accession_bank
                   WHERE  accbk_acc_num = dblink_acc_num
                          AND accbk_fdbcont_zdb_id = dblink_fdbcont_zdb_id
                          AND accbk_length IS NOT NULL);

-- update ACCESSION_BANK
UPDATE accession_bank
SET    accbk_length = (SELECT dblink_length
                       FROM   db_link
                       WHERE  dblink_acc_num = accbk_acc_num
                              AND dblink_fdbcont_zdb_id = accbk_fdbcont_zdb_id
                              AND dblink_length IS NOT NULL)
WHERE  accbk_length IS NULL
        AND EXISTS (SELECT 'x'
                   FROM   db_link
                   WHERE  accbk_acc_num = dblink_acc_num
                          AND accbk_fdbcont_zdb_id = dblink_fdbcont_zdb_id
                          AND dblink_length IS NOT NULL);