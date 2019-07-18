SELECT feature_zdb_id,
       mrkr_zdb_id,
       recattrib_source_zdb_id
FROM   feature,
       marker,
       feature_marker_relationship,
       record_attribution
WHERE  feature_zdb_id = fmrel_ftr_zdb_id
       AND fmrel_mrkr_zdb_id = mrkr_zdb_id
       AND fmrel_type = 'contains phenotypic sequence feature'
       AND mrkr_type = 'GTCONSTRCT'
       AND recattrib_data_zdb_id = fmrel_zdb_id;
