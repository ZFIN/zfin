SELECT feature_zdb_id,
  feature_name
FROM   feature
       WHERE NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship
                       WHERE  fmrel_ftr_zdb_id = feature_zdb_id
                              AND fmrel_type = 'created by')
       AND EXISTS (SELECT 'x'
                   FROM   feature_assay
                   WHERE  featassay_feature_zdb_id = feature_zdb_id
                          AND ( featassay_mutagen = 'TALEN'
                                OR featassay_mutagen = 'CRISPR' )) order by feature_zdb_id;