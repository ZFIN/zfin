SELECT feature.feature_zdb_id,
       feature.feature_abbrev,
       str.mrkr_zdb_id,
       str.mrkr_abbrev,
       fa.featassay_mutagen
FROM   feature AS feature,
       feature_marker_relationship,
       marker AS str,
       feature_assay AS fa
WHERE  fmrel_ftr_zdb_id = feature.feature_zdb_id
       AND fmrel_mrkr_zdb_id = str.mrkr_zdb_id
       AND str.mrkr_type = 'CRISPR'
       AND fa.featassay_mutagen != str.mrkr_type
       AND feature.feature_zdb_id = fa.featassay_feature_zdb_id
UNION
SELECT feature.feature_zdb_id,
       feature.feature_abbrev,
       str.mrkr_zdb_id,
       str.mrkr_abbrev,
       fa.featassay_mutagen
FROM   feature AS feature,
       feature_marker_relationship,
       marker AS str,
       feature_assay AS fa
WHERE  fmrel_ftr_zdb_id = feature.feature_zdb_id
       AND fmrel_mrkr_zdb_id = str.mrkr_zdb_id
       AND str.mrkr_type = 'TALEN'
       AND fa.featassay_mutagen != str.mrkr_type
       AND feature.feature_zdb_id = fa.featassay_feature_zdb_id