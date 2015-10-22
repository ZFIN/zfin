SELECT recattrib_data_zdb_id,
       recattrib_source_zdb_id
FROM   record_attribution
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
       AND get_obj_type(recattrib_data_zdb_id) NOT IN ( 'ORTHO', 'ALT', 'DBL INK' )
       AND NOT EXISTS (SELECT mrkr_zdb_id
                       FROM   marker
                       WHERE  recattrib_data_zdb_id = mrkr_zdb_id)
       AND NOT EXISTS (SELECT dalias_zdb_id
                       FROM   data_alias
                       WHERE  recattrib_data_zdb_id = dalias_zdb_id);