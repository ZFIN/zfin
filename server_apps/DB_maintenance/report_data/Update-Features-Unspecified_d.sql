SELECT feature_zdb_id,
       feature_name,
       mrkr_abbrev
FROM   feature,
       marker,
       feature_marker_relationship
WHERE  feature_zdb_id = fmrel_ftr_zdb_id
       AND mrkr_zdb_id = fmrel_mrkr_zdb_id
       AND feature_name LIKE '%_unspecified'
       AND feature_type = 'UNSPECIFIED'
       AND feature_name != mrkr_abbrev
           || '_unspecified'
UNION
SELECT feature_zdb_id,
       feature_name,
       mrkr_abbrev
FROM   feature,
       marker,
       feature_marker_relationship
WHERE  feature_zdb_id = fmrel_ftr_zdb_id
       AND mrkr_zdb_id = fmrel_mrkr_zdb_id
       AND feature_name LIKE '%_unrecovered'
       AND feature_name != mrkr_abbrev
           || '_unrecovered' ;

-- update feature_name column
UPDATE feature
SET    feature_name = (SELECT mrkr_abbrev||'_unspecified'

                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_name LIKE '%_unspecified')
WHERE  feature_name LIKE '%_unspecified'
       AND feature_type = 'UNSPECIFIED'
       AND NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship,
                              marker
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_name = mrkr_abbrev||'_unspecified');

-- update feature_abbrev column
UPDATE feature
SET    feature_abbrev = (SELECT mrkr_abbrev||'_unspecified'
                         FROM   marker,
                                feature_marker_relationship
                         WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                                AND feature_zdb_id = fmrel_ftr_zdb_id
                                AND feature_abbrev = mrkr_abbrev||'_unspecified')
WHERE  feature_name LIKE '%_unspecified'
       AND feature_type = 'UNSPECIFIED'
       AND NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship,
                              marker
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_abbrev = mrkr_abbrev||'_unspecified');

-- update feature_name column on unrecovered
UPDATE feature
SET    feature_name = (SELECT mrkr_abbrev||'_unrecovered'
                       FROM   marker,
                              feature_marker_relationship
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_name = mrkr_abbrev||'_unrecovered')
WHERE  feature_name LIKE '%_unrecovered'
       AND NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship,
                              marker
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_name = mrkr_abbrev||'_unrecovered');

-- update feature_abbrev column on unrecovered
UPDATE feature
SET    feature_abbrev = (SELECT mrkr_abbrev||'_unrecovered'
                         FROM   marker,
                                feature_marker_relationship
                         WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                                AND feature_zdb_id = fmrel_ftr_zdb_id
                                AND feature_abbrev LIKE '%_unrecovered')
WHERE  feature_name LIKE '%_unrecovered'
       AND NOT EXISTS (SELECT 'x'
                       FROM   feature_marker_relationship,
                              marker
                       WHERE  mrkr_zdb_id = fmrel_mrkr_zdb_id
                              AND feature_zdb_id = fmrel_ftr_zdb_id
                              AND feature_abbrev = mrkr_abbrev||'_unrecovered');