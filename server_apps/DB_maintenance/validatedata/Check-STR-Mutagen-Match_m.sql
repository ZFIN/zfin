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
       AND str.mrkr_type in ('CRISPR','TALEN')
       AND fa.featassay_mutagen not in ('DNA and CRISPR','DNA and TALEN')
       and fa.featassay_mutagen != str.mrkr_type
       AND feature.feature_zdb_id = fa.featassay_feature_zdb_id
       -- Suppress features where the only distinct pub(s) are from this ZKO pub list
       AND EXISTS (
           SELECT 1 FROM record_attribution
           WHERE recattrib_data_zdb_id = feature.feature_zdb_id
           AND recattrib_source_zdb_id NOT IN ('ZDB-PUB-190102-5', 'ZDB-PUB-200102-5', 'ZDB-PUB-210105-1', 'ZDB-PUB-220103-2', 'ZDB-PUB-230103-8', 'ZDB-PUB-240103-28', 'ZDB-PUB-250106-1', 'ZDB-PUB-260105-13')
       )
