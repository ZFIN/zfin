SELECT mrkr_zdb_id,
       mrkr_name,
       mrkr_abbrev
FROM   marker est
WHERE  mrkr_type = 'EST'
       AND NOT EXISTS (SELECT *
                       FROM   clone
                       WHERE  clone_mrkr_zdb_id = est.mrkr_zdb_id)
       AND NOT EXISTS (SELECT *
                       FROM   marker m1,
                              marker_relationship
                       WHERE  mrel_mrkr_1_zdb_id = m1.mrkr_zdb_id
                              AND mrel_mrkr_2_zdb_id = est.mrkr_zdb_id
                              AND mrel_type = 'gene encodes small segment'
                              AND m1.mrkr_name LIKE 'xx:%')
       AND mrkr_name NOT IN ( 'cb23', 'cb42', 'cb70', 'cb104',
                              'cb109', 'cb114' )
ORDER  BY mrkr_name;
