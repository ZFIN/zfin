SELECT recattrib_data_zdb_id,
       feature_name,
       feature_abbrev
FROM   record_attribution,
       feature
WHERE  recattrib_source_zdb_id = 'ZDB-PUB-030508-1'
       AND recattrib_data_zdb_id = feature_zdb_id;
