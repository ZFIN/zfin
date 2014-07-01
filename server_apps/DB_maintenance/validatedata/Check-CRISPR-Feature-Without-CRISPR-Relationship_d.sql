SELECT feature_zdb_id,
  featassay_mutagen,
  fmrel_mrkr_zdb_id
FROM   feature,
  feature_assay,
  feature_marker_relationship
WHERE  feature_zdb_id = featassay_feature_zdb_id
       AND feature_zdb_id = fmrel_ftr_zdb_id
       AND featassay_mutagen = 'CRISPR'
       AND ( fmrel_mrkr_zdb_id LIKE 'ZDB-TALEN%'
             OR fmrel_mrkr_zdb_id LIKE 'ZDB-MRPH%' )