SELECT dblink_acc_num
FROM   db_link
WHERE  dblink_acc_num LIKE 'zfin%'
       AND NOT EXISTS (SELECT 'x'
                       FROM   zfin_accession_number
                       WHERE  za_acc_num = dblink_acc_num);