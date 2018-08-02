SELECT mrkr_name,
       mrkr_zdb_id,
       NAME
FROM   marker,
       person
WHERE  mrkr_type IN ('TGCONSTRCT',
                     'PTCONSTRCT',
                     'ETCONSTRCT',
                     'GTCONSTRCT')
AND    mrkr_owner NOT IN ('ZDB-PERS-100329-1',
                          'ZDB-PERS-981201-7')
AND    Get_date_from_id(mrkr_zdb_id,'YYYYMMDD') >  to_char(CURRENT_DATE - interval '30 days', 'YYYYMMDD')
AND    zdb_id = mrkr_owner;