SELECT feature_zdb_id, feature_abbrev,recattrib_source_zdb_id
FROM   distinct feature, record_attribution
        WHERE  feature_type='MNV'
        and feature_zdb_id=recattrib_data_zdb_id
        order by feature_zdb_id;