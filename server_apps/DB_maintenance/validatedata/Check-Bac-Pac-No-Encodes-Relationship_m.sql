                                  SELECT mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id,
       mrkr_type,
       mrel_type
FROM   marker,
       marker_relationship,
       marker_relationship_type
WHERE  mrkr_type = 'BAC'
       AND ( mrkr_zdb_id = mrel_mrkr_2_zdb_id
             AND mrel_type = mreltype_name
             AND mreltype_2_to_1_comments = 'Is Encoded by' )
UNION
SELECT mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id,
       mrkr_type,
       mrel_type
FROM   marker,
       marker_relationship,
       marker_relationship_type
WHERE  mrkr_type = 'BAC'
       AND ( mrkr_zdb_id = mrel_mrkr_1_zdb_id
             AND mrel_type = mreltype_name
             AND mreltype_1_to_2_comments = 'Encodes' )
UNION
SELECT mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id,
       mrkr_type,
       mrel_type
FROM   marker,
       marker_relationship,
       marker_relationship_type
WHERE  mrkr_type = 'PAC'
       AND ( mrkr_zdb_id = mrel_mrkr_2_zdb_id
             AND mrel_type = mreltype_name
             AND mreltype_2_to_1_comments = 'Is Encoded by' )
UNION
SELECT mrel_mrkr_1_zdb_id,
       mrel_mrkr_2_zdb_id,
       mrkr_type,
       mrel_type
FROM   marker,
       marker_relationship,
       marker_relationship_type
WHERE  mrkr_type = 'PAC'
       AND ( mrkr_zdb_id = mrel_mrkr_1_zdb_id
             AND mrel_type = mreltype_name
             AND mreltype_1_to_2_comments = 'Encodes' );