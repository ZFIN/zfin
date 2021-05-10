SELECT recattrib_data_zdb_id,
       dblink_linked_recid,
       dblink_acc_num
FROM   record_attribution,
       db_link
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
       AND recattrib_data_zdb_id = dblink_zdb_id
UNION
SELECT recattrib_data_zdb_id,
       '',
       ''
FROM   record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
and recattrib_data_zdb_id like 'ZDB-ORTH%';
