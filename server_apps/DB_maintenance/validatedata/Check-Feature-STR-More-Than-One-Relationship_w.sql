SELECT feature_zdb_id,
       feature_name,
       fmrel_type
FROM   feature,
       feature_marker_relationship a
WHERE  feature_type = 'INDEL'
       AND fmrel_ftr_zdb_id = feature_zdb_id
       AND NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship b
                       WHERE  b.fmrel_zdb_id != a.fmrel_zdb_id)
       AND EXISTS (SELECT 'x'
                   FROM   feature_assay
                   WHERE  featassay_feature_zdb_id = feature_zdb_id
                          AND ( featassay_mutagen = 'TALEN'
                                 OR featassay_mutagen = 'CRISPR' ));