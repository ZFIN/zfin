SELECT mrkr_zdb_id,
       mrkr_abbrev
FROM   marker
WHERE  mrkr_type IN ( 'EST', 'CDNA', 'BAC', 'PAC', 'FOSMID' )
       AND NOT EXISTS (SELECT 'x'
                       FROM   clone
                       WHERE  clone_mrkr_zdb_id = mrkr_zdb_id);